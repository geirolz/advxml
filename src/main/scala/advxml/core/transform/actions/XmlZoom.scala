package advxml.core.transform.actions

import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.transform.actions.XmlZoom.{Filter, ImmediateDown, ZoomAction}
import cats.Alternative

import scala.xml.NodeSeq

sealed trait ZoomedNodeSeq {
  val nodeSeq: NodeSeq
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

  def apply[F[_]](wholeDocument: NodeSeq)(implicit F: Alternative[F]): F[ZoomedNodeSeq] = {

    case class ZoomedNodeSeqImpl(nodeSeq: NodeSeq, parents: List[NodeSeq]) extends ZoomedNodeSeq

    @scala.annotation.tailrec
    def rec(current: ZoomedNodeSeq, zActions: List[ZoomAction]): F[ZoomedNodeSeq] = {
      zActions.headOption match {
        case None => F.pure(current)
        case Some(f @ ImmediateDown(_)) if f.predicate(current.nodeSeq) =>
          rec(ZoomedNodeSeqImpl(f(current.nodeSeq), current.parents :+ current.nodeSeq), zActions.tail)
        case Some(f @ Filter(_)) =>
          rec(ZoomedNodeSeqImpl(f(current.nodeSeq), current.parents), zActions.tail)
        case _ => F.empty
      }
    }

    rec(ZoomedNodeSeqImpl(wholeDocument, Nil), this.zoomActions)
  }
}

object XmlZoom {

  lazy val empty: XmlZoom = XmlZoom(Nil)
  lazy val root: XmlZoom = empty

  sealed trait ZoomAction {
    def apply(ns: NodeSeq): NodeSeq
    val predicate: XmlPredicate = (apply _).andThen(_.nonEmpty)
  }
  final case class ImmediateDown(value: String) extends ZoomAction {
    def apply(ns: NodeSeq): NodeSeq = ns \ value
  }

  //TODO: not supported yet.
//  case class Down(value: String) extends ZoomAction {
//    def apply(ns: NodeSeq): NodeSeq = ns \\ value
//  }
  final case class Filter(p: XmlPredicate) extends ZoomAction {
    def apply(ns: NodeSeq): NodeSeq = ns.filter(p)
  }
}
