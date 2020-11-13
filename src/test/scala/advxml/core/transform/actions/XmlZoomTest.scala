package advxml.core.transform.actions

import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.transform.actions.XmlZoom.ImmediateDown
import advxml.core.transform.actions.XmlZoomTest.ContractFuncs
import advxml.implicits.XmlZoomOps
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
        immediateDown   = (z, n) => z.immediateDown(n),
        filter          = (z, p) => z.filter(p),
        find            = (z, p) => z.find(p),
        atIndex         = (z, idx) => z.atIndex(idx),
        head            = _.head(),
        last            = _.last()
      )
      // format: on
    )
    .runAll()
}

object XmlZoomTest {

  import advxml.syntax._
  import advxml.instances.convert._

  case class ContractFuncs(
    immediateDown: (XmlZoom, String) => XmlZoom,
    filter: (XmlZoom, XmlPredicate) => XmlZoom,
    find: (XmlZoom, XmlPredicate) => XmlZoom,
    atIndex: (XmlZoom, Int) => XmlZoom,
    head: XmlZoom => XmlZoom,
    last: XmlZoom => XmlZoom
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("XmlZoom", subDesc) {

    import advxml.instances.transform._
    import advxml.testUtils.ScalacticXmlEquality._
    import cats.instances.try_._

    test("immediateDownTest") {
      assert(f.immediateDown(root, "N1").zoomActions == List(ImmediateDown("N1")))
    }

    test("applyWithImmediateDownTest") {
      val doc: Elem = <Root>
        <N1 T1="V1"/>
        <N1 T2="V2"/>
      </Root>
      val xmlZoom: XmlZoom = f.immediateDown(root, "N1")
      val result: Try[ZoomedNodeSeq] = xmlZoom(doc)
      assert(result.get.nodeSeq(0) === <N1 T1="V1"/>)
      assert(result.get.nodeSeq(1) === <N1 T2="V2"/>)
    }

    test("filterTest") {
      val xml =
        <foo>
          <bar id="1" />
          <bar id="2" />
          <bar id="1" />
        </foo>

      val xmlZoom: XmlZoom = f.filter(root \ "bar", attrs(k"id" === "1"))
      val value: ZoomedNodeSeq = xmlZoom(xml).get

      assert(value.nodeSeq === NodeSeq.fromSeq(Seq(<bar id="1"/>, <bar id="1"/>)))
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("findTest") {
      val xml =
        <foo>
          <bar id="1" />
          <bar id="2" />
          <bar id="1" />
        </foo>

      val xmlZoom: XmlZoom = f.find(root \ "bar", attrs(k"id" === "1"))
      val value: ZoomedNodeSeq = xmlZoom(xml).get

      assert(value.nodeSeq === <bar id="1"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("atIndex") {
      val xml =
        <foo>
          <bar id="1" />
          <bar id="2" />
          <bar id="3" />
        </foo>

      val xmlZoom: XmlZoom = f.atIndex(root \ "bar", 2)
      val value: ZoomedNodeSeq = xmlZoom(xml).get

      assert(value.nodeSeq === <bar id="3"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("head") {
      val xml =
        <foo>
          <bar id="1" />
          <bar id="2" />
          <bar id="3" />
        </foo>

      val xmlZoom: XmlZoom = f.head(root \ "bar")
      val value: ZoomedNodeSeq = xmlZoom(xml).get

      assert(value.nodeSeq === <bar id="1"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }

    test("last") {
      val xml =
        <foo>
          <bar id="1" />
          <bar id="2" />
          <bar id="3" />
        </foo>

      val xmlZoom: XmlZoom = f.last(root \ "bar")
      val value: ZoomedNodeSeq = xmlZoom(xml).get

      assert(value.nodeSeq === <bar id="3"/>)
      assert(value.parents.size == 1)
      assert(value.parents.head === xml)
    }
  }
}
