package advxml.core.transform.actions

import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.transform.actions.XmlZoom.{Filter, ImmediateDown, ZoomAction}
import cats.Alternative

import scala.xml.NodeSeq

sealed trait ZoomedNode {
  val node: NodeSeq
  val parents: List[NodeSeq]
}

case class XmlZoom private (zoomActions: List[ZoomAction]) {

  def immediateDown(nodeName: String): XmlZoom =
    XmlZoom(zoomActions :+ ImmediateDown(nodeName))

  def andThen(that: XmlZoom): XmlZoom =
    XmlZoom(zoomActions ++ that.zoomActions)

  def andThenAll(that: List[XmlZoom]): XmlZoom =
    that.foldLeft(this)((a, b) => a.andThen(b))

  def filter(p: XmlPredicate): XmlZoom =
    XmlZoom(zoomActions :+ Filter(p))

  def apply[F[_]](wholeDocument: NodeSeq)(implicit F: Alternative[F]): F[ZoomedNode] = {

    case class ZoomedNodeImpl(node: NodeSeq, parents: List[NodeSeq]) extends ZoomedNode

    @scala.annotation.tailrec
    def rec(current: ZoomedNode, zActions: List[ZoomAction]): F[ZoomedNode] = {
      zActions.headOption match {
        case None => F.pure(current)
        case Some(f @ ImmediateDown(_)) if f.predicate(current.node) =>
          rec(ZoomedNodeImpl(f(current.node), current.parents :+ current.node), zActions.tail)
        case Some(f @ Filter(_)) =>
          rec(ZoomedNodeImpl(f(current.node), current.parents), zActions.tail)
        case _ => F.empty
      }
    }

    rec(ZoomedNodeImpl(wholeDocument, Nil), this.zoomActions)
  }
}

object XmlZoom {

  val root: XmlZoom = XmlZoom(Nil)

  sealed trait ZoomAction {
    def apply(ns: NodeSeq): NodeSeq
    val predicate: XmlPredicate = (apply _).andThen(_.nonEmpty)
  }
  case class ImmediateDown(value: String) extends ZoomAction {
    def apply(ns: NodeSeq): NodeSeq = ns \ value
  }

  //TODO: not supported yet.
//  case class Down(value: String) extends ZoomAction {
//    def apply(ns: NodeSeq): NodeSeq = ns \\ value
//  }
  case class Filter(p: XmlPredicate) extends ZoomAction {
    def apply(ns: NodeSeq): NodeSeq = ns.filter(p)
  }
}
