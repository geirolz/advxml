package advxml.core.transform

import advxml.core.{ErrorHandler, ExHandler}
import advxml.core.data.{error, Converter, StringTo, XmlPredicate}
import advxml.core.transform.XmlZoom.{ZoomAction, _}
import cats.{Applicative, FlatMap, Monad}

import scala.language.dynamics
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

//###################### NODES ######################
private[advxml] sealed trait XmlZoomNodeBase extends Dynamic {

  type Type

  val actions: List[ZoomAction]

  def add(action: ZoomAction, actions: ZoomAction*): Type =
    addAll((action +: actions).toList)

  def addAll(action: List[ZoomAction]): Type

  def bind(ns: NodeSeq): XmlZoomBinded

  def unbind(): XmlZoom

  //######################## ACTIONS ################################
  def immediateDown(nodeName: String): Type =
    this.add(ImmediateDown(nodeName))

  def filter(p: XmlPredicate): Type =
    this.add(Filter(p))

  def find(p: XmlPredicate): Type =
    this.add(Find(p))

  def atIndex(idx: Int): Type =
    this.add(AtIndex(idx))

  def head(): Type =
    this.add(Head)

  def last(): Type =
    this.add(Last)

  //######################## DYNAMIC ################################
  def applyDynamic(nodeName: String)(idx: Int): Type =
    this.addAll(List(ImmediateDown(nodeName), AtIndex(idx)))

  def selectDynamic(nodeName: String): Type =
    this.add(ImmediateDown(nodeName))
}

sealed trait XmlZoom extends XmlZoomNodeBase {

  override type Type = XmlZoom

  def raw[F[_]: Monad: ExHandler](document: NodeSeq): F[NodeSeq] =
    Monad[F].map(run(document))(_.nodeSeq)

  def run[F[_]: Monad: ExHandler](document: NodeSeq): F[XmlZoomResult]
}

sealed trait XmlZoomBinded extends XmlZoomNodeBase {

  override type Type = XmlZoomBinded

  val document: NodeSeq

  def raw[F[_]: Monad: ExHandler]: F[NodeSeq] =
    Monad[F].map(run)(_.nodeSeq)

  def run[F[_]: Monad: ExHandler]: F[XmlZoomResult]
}

sealed trait XmlZoomResult {
  val nodeSeq: NodeSeq
  val parents: List[NodeSeq]
}

/** [[XmlZoom]] is a powerful system that allow to "zoom" inside a [[NodeSeq]] and select one or more elements keeping
  * all step list and return a monadic value to handle possible errors.
  *
  * <h4>HOW TO USE</h4>
  * [[XmlZoom]] is based on three types:
  * - [[XmlZoom]] a.k.a XmlZoomUnbinded
  * - [[XmlZoomBinded]]
  * - [[XmlZoomResult]]
  *
  * <b>XmlZoom</b>
  * Is the representation of unbind zoom instance. It contains only the list of the actions to run on a [[NodeSeq]].
  *
  * <b>XmlZoomBinded</b>
  * Is the representation of binded zoom instance. Binded because it contains both [[ZoomAction]] and [[NodeSeq]] target.
  *
  * <b>XmlZoomResult</b>
  * Is the result of the [[XmlZoom]], that contains selected [[NodeSeq]] and his parents.
  */
object XmlZoom {

  //########################### INIT ###############################
  /** Create a new unbinded [[XmlZoom]] with specified actions.
    *
    * @param actions actions list for the zooming action
    * @return new instance of unbinded [[XmlZoom]]
    */
  def apply(actions: List[ZoomAction]): XmlZoom = Impls.Unbinded(actions)

  /** Empty unbinded [[XmlZoom]] instance, without any [[ZoomAction]]
    */
  lazy val empty: XmlZoom = apply(Nil)

  /** Just an alias for [[XmlZoom]], to use when you are building and XmlZoom that starts from the root.
    */
  lazy val root: XmlZoom = empty

  /** Just a binded alias for [[XmlZoom]], to use when you are building and XmlZoom that starts from the root.
    */
  def root(document: NodeSeq): XmlZoomBinded = root.bind(document)

  /** Just an alias for Root, to use when you are building and XmlZoom that not starts from the root for the document.
    * It's exists just to clarify the code.
    * If your [[XmlZoom]] starts for the root of the document please use [[root]]
    */
  lazy val $ : XmlZoom = empty

  /** Just a binded alias for root, to use when you are building and XmlZoom that not starts from the root for the document.
    * It's exists just to clarify the code.
    * If your [[XmlZoom]] starts for the root of the document please use [[root]]
    */
  def $(document: NodeSeq): XmlZoomBinded = $.bind(document)

  //########################### IMPLS ###############################
  private object Impls {

    case class Unbinded(actions: List[ZoomAction]) extends XmlZoom {
      $thisZoom =>

      override def addAll(that: List[ZoomAction]): Type = copy(actions = actions ++ that)

      override def bind(ns: NodeSeq): XmlZoomBinded = Binded(ns, actions)

      override def unbind(): XmlZoom = this

      def run[F[_]: Monad: ExHandler](document: NodeSeq): F[XmlZoomResult] =
        bind(document).run[F]
    }

    case class Binded(document: NodeSeq, actions: List[ZoomAction]) extends XmlZoomBinded {
      $thisZoom =>

      override def addAll(that: List[ZoomAction]): Type = copy(actions = actions ++ that)

      override def bind(that: NodeSeq): Type = copy(document = that)

      override def unbind(): XmlZoom = Unbinded(actions)

      def run[F[_]: Monad: ExHandler]: F[XmlZoomResult] = {

        @scala.annotation.tailrec
        def rec(current: XmlZoomResult, zActions: List[ZoomAction], logPath: String): Try[XmlZoomResult] = {
          zActions.headOption match {
            case None => Success(current)
            case Some(action) =>
              val newParents = action match {
                case ImmediateDown(_) => current.parents :+ current.nodeSeq
                case _                => current.parents
              }

              action(current.nodeSeq) match {
                case Some(value) => rec(Impls.Result(value, newParents), zActions.tail, logPath + action.symbol)
                case None        => Failure(error.ZoomFailedException($thisZoom, action, logPath))
              }
          }
        }

        ErrorHandler.fromTry(rec(Impls.Result(document, Nil), this.actions, "root"))
      }
    }

    case class Result(nodeSeq: NodeSeq, parents: List[NodeSeq]) extends XmlZoomResult
  }

  //########################### NODES ACTIONS ###############################
  sealed trait ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq]

    val symbol: String

    def +(that: ZoomAction): XmlZoom = ++(List(that))

    def ++(that: List[ZoomAction]): XmlZoom = Impls.Unbinded(this +: that)

    protected val checkEmpty: NodeSeq => Option[NodeSeq] = {
      case x if x.isEmpty => None
      case x              => Some(x)
    }
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

object XmlContentZoom {

  import cats.implicits._

  //************************************ ATTRIBUTE *************************************
  def attr[F[_]: Monad: ExHandler, T: StringTo[F, *]](ns: NodeSeq, key: String): F[T] =
    attrM(Applicative[F].pure(ns), key)

  def attrM[F[_]: FlatMap: ExHandler, T: StringTo[F, *]](ns: F[NodeSeq], key: String): F[T] =
    ns.map(_ \@ key).flatMap(check[F, T](_, new RuntimeException(s"Missing/Empty $key attribute.")))

  //*************************************** TEXT  **************************************
  def text[F[_]: Monad: ExHandler, T: StringTo[F, *]](ns: NodeSeq): F[T] =
    textM(Applicative[F].pure(ns))

  def textM[F[_]: FlatMap: ExHandler, T: StringTo[F, *]](ns: F[NodeSeq]): F[T] =
    ns.map(_.text).flatMap(check[F, T](_, new RuntimeException(s"Missing/Empty text.")))

  private def check[F[_]: FlatMap: ExHandler, T](value: String, error: => Throwable)(implicit
    c: Converter[F, String, T]
  ): F[T] = {
    ErrorHandler
      .fromOption(error)(
        value match {
          case "" => None
          case x  => Some(x)
        }
      )
      .flatMap(c.apply)
  }
}
