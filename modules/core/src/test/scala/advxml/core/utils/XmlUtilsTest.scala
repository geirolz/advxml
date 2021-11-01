package advxml.core.utils

import org.scalatest.funsuite.AnyFunSuite

import scala.xml.{Elem, Node, Text}

class XmlUtilsTest extends AnyFunSuite {

  import advxml.testUtils.ScalacticXmlEquality._

  test("XmlUtils.nodeToElem") {

    val node: Node = <Node n="1"><Child n="2" />TEST</Node>
    val elem       = XmlUtils.nodeToElem(node)

    assert(elem === <Node n="1"><Child n="2" />TEST</Node>)
  }

  test("XmlUtils.flatMapChildren") {

    val node: Elem =
      <a>
        <b>1</b>
        <b>2</b>
        <b>3</b>
      </a>
    val result = XmlUtils.flatMapChildren(node, n => <c>{n.text}</c>)

    assert(
      result ===
        <a>
        <c>1</c>
        <c>2</c>
        <c>3</c>
      </a>
    )
  }

  test("XmlUtils.emptyText") {

    val textEmpty: Text          = Text("")
    val textEmptyWithSpace: Text = Text(" ")
    val textNonEmpty: Text       = Text("test")

    assert(XmlUtils.emptyText(textEmpty))
    assert(XmlUtils.emptyText(textEmptyWithSpace))
    assert(!XmlUtils.emptyText(textNonEmpty))
  }
}
