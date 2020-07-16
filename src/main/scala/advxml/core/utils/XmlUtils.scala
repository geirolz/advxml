package advxml.core.utils

import scala.xml.{Elem, Node, NodeSeq, Text}

//TODO: Create a syntax for these methods ?
object XmlUtils {

  def nodeToElem(n: Node): Elem =
    Elem(null, n.label, n.attributes, n.scope, false, n.child: _*)

  def flatMapChildren(e: Elem, f: Node => NodeSeq): Elem = {
    val updatedChildren: Seq[Node] = e.child.filterNot(emptyText).flatMap {
      case n: Node => f(n)
      case o       => o
    }

    (e match {
      case el: Elem   => el
      case node: Node => nodeToElem(node)
    }).copy(child = updatedChildren)
  }

  def emptyText(n: Node): Boolean = n match {
    case t: Text => t.text.trim.isEmpty
    case _       => false
  }
}
