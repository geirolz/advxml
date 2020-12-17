package advxml.core.transform

import advxml.core.data.XmlPredicate
import advxml.core.transform.XmlZoom.Down
import advxml.core.transform.XmlZoomTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalactic.TypeCheckedTripleEquals.convertToCheckingEqualizer
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlZoomTest extends AnyFunSuite with FunSuiteContract {

  XmlZoomTest
    .Contract(
      // format: off
      f = ContractFuncs(
        down          = (z, n) => z.down(n),
        filter        = (z, p) => z.filter(p),
        find          = (z, p) => z.find(p),
        atIndex       = (z, idx) => z.atIndex(idx),
        head          = _.head(),
        last          = _.last()
      )
      // format: on
    )
    .runAll()

  test("Append + ZoomAction") {
    val zoom: XmlZoom = Down("N1") + Down("N2")
    assert(zoom.actions == List(Down("N1"), Down("N2")))
  }

  test("Append + List[ZoomAction]") {
    val zoom: XmlZoom = Down("N1") ++ List(Down("N2"), Down("N3"))
    assert(zoom.actions == List(Down("N1"), Down("N2"), Down("N3")))
  }
}

object XmlZoomTest {

  import advxml.instances.convert._
  import advxml.syntax._

  case class ContractFuncs(
    down: (XmlZoom, String) => XmlZoom,
    filter: (XmlZoom, XmlPredicate) => XmlZoom,
    find: (XmlZoom, XmlPredicate) => XmlZoom,
    atIndex: (XmlZoom, Int) => XmlZoom,
    head: XmlZoom => XmlZoom,
    last: XmlZoom => XmlZoom
  )

  //noinspection ZeroIndexToHead
  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("XmlZoom", subDesc) {

    import advxml.instances.transform._
    import advxml.syntax.transform._
    import advxml.testUtils.ScalacticXmlEquality._
    import cats.instances.try_._

    test("BindedXmlZoom to UnbindedXmlZoom") {
      val doc: Elem = <Root></Root>
      val xmlZoom: BindedXmlZoom = $(doc).unbind().unbind().bind(doc).bind(doc)
      assert(xmlZoom.document === doc)
    }

    test("BindedXmlZoom $") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: BindedXmlZoom = $(doc)
      assert(xmlZoom.document === doc)
    }

    test("BindedXmlZoom root") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: BindedXmlZoom = root(doc)
      assert(xmlZoom.document === doc)
    }

    test("UnbindedXmlZoom.detailed") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: XmlZoom = f.down(root, "N1")
      val result: Try[XmlZoomResult] = xmlZoom.detailed[Try](doc)
      assert(result.get.nodeSeq.head === <N1 T1="V1"/>)
    }

    test("UnbindedXmlZoom.run") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: XmlZoom = f.down(root, "N1")
      val result: Try[NodeSeq] = xmlZoom.run[Try](doc)
      assert(result.get.head === <N1 T1="V1"/>)
    }

    test("BindedXmlZoom.detailed") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: XmlZoom = f.down(root, "N1")
      val result: Try[XmlZoomResult] = xmlZoom.bind(doc).detailed[Try]
      assert(result.get.nodeSeq.head === <N1 T1="V1"/>)
    }

    test("BindedXmlZoom.run") {
      val doc: Elem = <Root><N1 T1="V1"/></Root>
      val xmlZoom: XmlZoom = f.down(root, "N1")
      val result: Try[NodeSeq] = xmlZoom.bind(doc).run[Try]
      assert(result.get.head === <N1 T1="V1"/>)
    }

    test("downTest") {
      assert(f.down(root, "N1").actions == List(Down("N1")))
    }

    test("applyWithDownTest") {
      val doc: Elem = <Root>
        <N1 T1="V1"/>
        <N1 T2="V2"/>
      </Root>
      val xmlZoom: XmlZoom = f.down(root, "N1")
      val result: Try[XmlZoomResult] = xmlZoom.detailed[Try](doc)
      assert(result.get.nodeSeq(0) === <N1 T1="V1"/>)
      assert(result.get.nodeSeq(1) === <N1 T2="V2"/>)
    }

    test("filterTest") {
      val xml =
        <foo>
          <bar id="1"/>
          <bar id="2"/>
          <bar id="1"/>
        </foo>

      val xmlZoom: XmlZoom = f.filter(root / "bar", attrs(k"id" === "1"))
      val value: XmlZoomResult = xmlZoom.detailed[Try](xml).get

      assert(value.nodeSeq === NodeSeq.fromSeq(Seq(<bar id="1"/>, <bar id="1"/>)))
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("findTest") {
      val xml =
        <foo>
          <bar id="1"/>
          <bar id="2"/>
          <bar id="1"/>
        </foo>

      val xmlZoom: XmlZoom = f.find(root / "bar", attrs(k"id" === "1"))
      val value: XmlZoomResult = xmlZoom.detailed[Try](xml).get

      assert(value.nodeSeq === <bar id="1"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("atIndex") {
      val xml =
        <foo>
          <bar id="1"/>
          <bar id="2"/>
          <bar id="3"/>
        </foo>

      val xmlZoom: XmlZoom = f.atIndex(root / "bar", 2)
      val value: XmlZoomResult = xmlZoom.detailed[Try](xml).get

      assert(value.nodeSeq === <bar id="3"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("head") {
      val xml =
        <foo>
          <bar id="1"/>
          <bar id="2"/>
          <bar id="3"/>
        </foo>

      val xmlZoom: XmlZoom = f.head(root / "bar")
      val value: XmlZoomResult = xmlZoom.detailed[Try](xml).get

      assert(value.nodeSeq === <bar id="1"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("last") {
      val xml =
        <foo>
          <bar id="1"/>
          <bar id="2"/>
          <bar id="3"/>
        </foo>

      val xmlZoom: XmlZoom = f.last(root / "bar")
      val value: XmlZoomResult = xmlZoom.detailed[Try](xml).get

      assert(value.nodeSeq === <bar id="3"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }
  }
}
