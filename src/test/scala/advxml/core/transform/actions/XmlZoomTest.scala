package advxml.core.transform.actions

import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.transform.actions.XmlZoom.{Filter, ImmediateDown}
import advxml.core.transform.actions.XmlZoomTest.ContractFuncs
import advxml.instances.transform.{>, label, root}
import advxml.test.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

class XmlZoomTest extends AnyFunSuite with FunSuiteContract {
  XmlZoomTest
    .Contract(
      f = ContractFuncs(
        immediateDownAction = (z, n) => z.immediateDown(n),
        filterAction = (z, p) => z.filter(p)
      )
    )
    .runAll()
}

object XmlZoomTest {

  case class ContractFuncs(
    immediateDownAction: (XmlZoom, String) => XmlZoom,
    filterAction: (XmlZoom, XmlPredicate) => XmlZoom
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("XmlZoom", subDesc) {

    test("immediateDownTest") {
      assert(f.immediateDownAction(root, "N1").zoomActions == List(ImmediateDown("N1")))
    }

    test("andThenWithImmediateDownTest") {
      val xmlZoom1: XmlZoom = f.immediateDownAction(root, "N1")
      val xmlZoom2: XmlZoom = f.immediateDownAction(>, "N2")
      val result: XmlZoom = xmlZoom1.andThen(xmlZoom2)

      assert(result.zoomActions.size == 2)
      assert(result.zoomActions.head == ImmediateDown("N1"))
      assert(result.zoomActions.last == ImmediateDown("N2"))
    }

    test("andThenAllWithImmediateDownTest") {
      val xmlZoom1: XmlZoom = f.immediateDownAction(root, "N1")
      val zooms: List[XmlZoom] = List(f.immediateDownAction(>, "N2"), f.immediateDownAction(>, "N3"))
      val result: XmlZoom = xmlZoom1.andThenAll(zooms)

      assert(result.zoomActions.size == 3)
      assert(result.zoomActions.head == ImmediateDown("N1"))
      assert(result.zoomActions(1) == ImmediateDown("N2"))
      assert(result.zoomActions.last == ImmediateDown("N3"))
    }

    test("applyWithImmediateDownTest") {

      import advxml.instances.transform._
      import advxml.syntax.normalize._
      import cats.instances.option._

      val doc: Elem = <Root>
        <N1 T1="V1"/>
      </Root>
      val xmlZoom: XmlZoom = f.immediateDownAction(root, "N1")
      val result: Option[ZoomedNode] = xmlZoom(doc)
      assert(result.get.node |==| <N1 T1="V1"/>)
    }

    test("filterTest") {
      val predicate: XmlPredicate = label(_ == "N1")
      val xmlZoom: XmlZoom = f.filterAction(root, predicate)

      assert(xmlZoom.zoomActions.size == 1)
      assert(xmlZoom.zoomActions.head == Filter(predicate))
    }
  }
}
