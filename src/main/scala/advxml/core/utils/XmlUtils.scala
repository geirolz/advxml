package advxml.core.utils

import scala.xml.{Elem, Node, NodeSeq}

object XmlUtils {

  def nodeToElem(n: Node): Elem =
    Elem(null, n.label, n.attributes, n.scope, false, n.child: _*)

  def diffChildren(n1: Node, n2: NodeSeq): NodeSeq =
    n1.child.filter {
      case elem: Elem => !n2.contains(elem)
      case _          => true
    }

  def flatMapChildren(e: Elem, f: Node => NodeSeq): NodeSeq = {
    val updatedChildren = e.child.flatMap {
      case n: Node => f(n)
      case o       => o
    }

    e.copy(child = updatedChildren)
  }
}
