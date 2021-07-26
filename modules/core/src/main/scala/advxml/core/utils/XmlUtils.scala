package advxml.core.utils

import scala.xml.{Elem, Node, NodeSeq, Text}

//TODO: Create a syntax for these methods ?
object XmlUtils {

  val prettyPrinter = new scala.xml.PrettyPrinter(80, 4)

  def prettyPrint(xml: NodeSeq): String =
    prettyPrinter.formatNodes(xml)

  def nodeToElem(n: Node): Elem =
    Elem(null, n.label, n.attributes, n.scope, false, n.child: _*)

  def flatMapChildren(e: Elem, f: Node => NodeSeq): Elem = {
    val updatedChildren: Seq[Node] = e.child.filterNot(emptyText).flatMap(f)

    e.copy(child = updatedChildren)
  }

  def emptyText(n: Node): Boolean = n match {
    case t: Text => t.text.trim.isEmpty
    case _       => false
  }
}
