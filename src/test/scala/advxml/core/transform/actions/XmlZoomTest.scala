package advxml.core.transform.actions

import advxml.core.transform.actions.XmlZoom.{Filter, ImmediateDown}
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

class XmlZoomTest extends AnyFunSuite {

  import advxml.instances.transform._
  import advxml.syntax.normalize._
  import cats.instances.option._

  test("Test immediateDown") {
    val xmlZoom = root immediateDown "N1"
    assert(xmlZoom.zoomActions == List(ImmediateDown("N1")))
  }

  test("Test andThen") {
    val xmlZoom1 = root immediateDown "N1"
    val xmlZoom2 = > immediateDown "N2"
    val result = xmlZoom1.andThen(xmlZoom2)

    assert(result.zoomActions.size == 2)
    assert(result.zoomActions.head == ImmediateDown("N1"))
    assert(result.zoomActions.last == ImmediateDown("N2"))
  }

  test("Test andThenAll") {
    val xmlZoom1 = root immediateDown "N1"
    val zooms = List(
      > immediateDown "N2",
      > immediateDown "N3"
    )
    val result = xmlZoom1.andThenAll(zooms)

    assert(result.zoomActions.size == 3)
    assert(result.zoomActions.head == ImmediateDown("N1"))
    assert(result.zoomActions(1) == ImmediateDown("N2"))
    assert(result.zoomActions.last == ImmediateDown("N3"))
  }

  test("Test filter") {
    val predicate = label(_ == "N1")
    val xmlZoom = root filter predicate

    assert(xmlZoom.zoomActions.size == 1)
    assert(xmlZoom.zoomActions.head == Filter(predicate))
  }

  test("Test apply") {
    val doc: Elem = <Root><N1 T1="V1"/></Root>
    val xmlZoom = root immediateDown "N1"
    val result: Option[ZoomedNode] = xmlZoom(doc)
    assert(result.get.node === <N1 T1="V1"/>)
  }
}
