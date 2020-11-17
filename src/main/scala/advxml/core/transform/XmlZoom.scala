package advxml.core.transform

import advxml.core.MonadEx
import advxml.core.data.XmlPredicate
import advxml.core.transform.XmlZoom._
import advxml.core.transform.exceptions.ZoomFailedException

import scala.language.dynamics
import scala.xml.NodeSeq

sealed trait ZoomResult {
  val nodeSeq: NodeSeq
  val parents: List[NodeSeq]
}

case class XmlZoom private (actions: List[ZoomAction]) extends Dynamic {
  $thisZoom =>

  def immediateDown(nodeName: String): XmlZoom =
    XmlZoom(actions :+ ImmediateDown(nodeName))

  def filter(p: XmlPredicate): XmlZoom =
    XmlZoom(actions :+ Filter(p))

  def find(p: XmlPredicate): XmlZoom =
    XmlZoom(actions :+ Find(p))

  def atIndex(idx: Int): XmlZoom =
    XmlZoom(actions :+ AtIndex(idx))

  def head(): XmlZoom =
    XmlZoom(actions :+ Head)

  def last(): XmlZoom =
    XmlZoom(actions :+ Last)

  def applyDynamic[T](nodeName: String)(idx: Int): XmlZoom =
    immediateDown(nodeName).atIndex(idx)

  def selectDynamic(nodeName: String): XmlZoom =
    immediateDown(nodeName)

  def apply[F[_]](wholeDocument: NodeSeq)(implicit F: MonadEx[F]): F[ZoomResult] = {

    case class ZoomResultImpl(nodeSeq: NodeSeq, parents: List[NodeSeq]) extends ZoomResult

    @scala.annotation.tailrec
    def rec(current: ZoomResult, zActions: List[ZoomAction], logPath: String): F[ZoomResult] = {
      zActions.headOption match {
        case None => F.pure(current)
        case Some(action) =>
          val newParents = action match {
            case ImmediateDown(_) => current.parents :+ current.nodeSeq
            case _                => current.parents
          }

          action(current.nodeSeq) match {
            case Some(value) => rec(ZoomResultImpl(value, newParents), zActions.tail, logPath + action.symbol)
            case None        => F.raiseError(ZoomFailedException(wholeDocument, $thisZoom, action, logPath))
          }
      }
    }

    rec(ZoomResultImpl(wholeDocument, Nil), this.actions, "root")
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

    val symbol: String
  }

  sealed trait FilterZoomAction extends ZoomAction

  final case class ImmediateDown(value: String) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns \ value)

    val symbol: String = s"/$value"
  }

  final case class Filter(p: XmlPredicate) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns.filter(p))

    val symbol: String = s"| $p"
  }

  final case class Find(p: XmlPredicate) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.find(p)

    val symbol: String = s"find($p)"
  }

  final case class AtIndex(idx: Int) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lift(idx)

    val symbol: String = s"($idx)"
  }

  final case object Head extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.headOption

    val symbol: String = s"head()"
  }

  final case object Last extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lastOption

    val symbol: String = s"last()"
  }
}
