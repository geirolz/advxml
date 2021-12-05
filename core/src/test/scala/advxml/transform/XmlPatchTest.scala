package advxml.transform

import advxml.transform.XmlPatch.NodeSeqPatchMap
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

class XmlPatchTest extends AnyFunSuite {

  test("XmlPatch.id") {
    val ns: Elem                = <node></node>
    val result: NodeSeqPatchMap = XmlPatch.id(ns).zipWithUpdated
    assert(result.head == ((Some(<node></node>), Some(<node></node>))))
  }

  test("XmlPatch.const") {
    val ns: Elem                = <node></node>
    val result: NodeSeqPatchMap = XmlPatch.const(ns, <updated></updated>).zipWithUpdated
    assert(result.head == ((Some(<node></node>), Some(<updated></updated>))))
  }

  test("XmlPatch.apply") {
    val ns: Elem = <node>10</node>
    val result: NodeSeqPatchMap =
      XmlPatch(
        ns,
        oldNode => <updated>{(oldNode.text.toInt + 10).toString}</updated>
      ).zipWithUpdated

    assert(result.head == ((Some(<node>10</node>), Some(<updated>20</updated>))))
  }
}
