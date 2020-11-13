package advxml.core.transform.actions

import advxml.core.XmlPredicate
import advxml.core.transform.actions.XmlZoom._
import advxml.core.MonadEx

import scala.language.dynamics
import scala.xml.NodeSeq

sealed trait ZoomedNodeSeq {
  val nodeSeq: NodeSeq
  val parents: List[NodeSeq]
}

case class XmlZoom private (zoomActions: List[ZoomAction]) extends Dynamic {

  def immediateDown(nodeName: String): XmlZoom =
    XmlZoom(zoomActions :+ ImmediateDown(nodeName))

  def filter(p: XmlPredicate): XmlZoom =
    XmlZoom(zoomActions :+ Filter(p))

  def find(p: XmlPredicate): XmlZoom =
    XmlZoom(zoomActions :+ Find(p))

  def atIndex(idx: Int): XmlZoom =
    XmlZoom(zoomActions :+ AtIndex(idx))

  def head(): XmlZoom =
    XmlZoom(zoomActions :+ Head)

  def last(): XmlZoom =
    XmlZoom(zoomActions :+ Last)

  def applyDynamic[T](nodeName: String)(idx: Int): XmlZoom =
    immediateDown(nodeName).atIndex(idx)

  def selectDynamic(nodeName: String): XmlZoom =
    immediateDown(nodeName)

  def apply[F[_]](wholeDocument: NodeSeq)(implicit F: MonadEx[F]): F[ZoomedNodeSeq] = {

    case class ZoomedNodeSeqImpl(nodeSeq: NodeSeq, parents: List[NodeSeq]) extends ZoomedNodeSeq

    @scala.annotation.tailrec
    def rec(current: ZoomedNodeSeq, zActions: List[ZoomAction]): F[ZoomedNodeSeq] = {
      zActions.headOption match {
        case None => F.pure(current)
        case Some(action) =>
          val newParents = action match {
            case ImmediateDown(_) => current.parents :+ current.nodeSeq
            case _                => current.parents
          }

          action(current.nodeSeq) match {
            case Some(value) => rec(ZoomedNodeSeqImpl(value, newParents), zActions.tail)
            case None        => F.raiseError(new RuntimeException("Empty Target"))
          }
      }
    }

    rec(ZoomedNodeSeqImpl(wholeDocument, Nil), this.zoomActions)
  }
}

object XmlZoom {

  lazy val empty: XmlZoom = XmlZoom(Nil)
  lazy val root: XmlZoom = empty

  val checkEmpty: NodeSeq => Option[NodeSeq] = {
    case x if x.isEmpty => None
    case x              => Some(x)
  }

  sealed trait ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq]
  }

  final case class ImmediateDown(value: String) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns \ value)
  }

  final case class Filter(p: XmlPredicate) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns.filter(p))
  }

  final case class Find(p: XmlPredicate) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.find(p)
  }

  final case class AtIndex(idx: Int) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lift(idx)
  }

  final case object Head extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.headOption
  }

  final case object Last extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lastOption
  }
}
