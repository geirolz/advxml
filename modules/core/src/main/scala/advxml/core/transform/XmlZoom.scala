package advxml.core.transform
import advxml.core.data._
import advxml.core.transform.XmlZoom.{ZoomAction, _}
import advxml.core.ApplicativeThrowOrEu
import cats.{FlatMap, Functor}

import scala.annotation.tailrec
import scala.language.dynamics
import scala.xml.{Node, NodeSeq}

//============================== NODES ==============================
private[advxml] sealed trait XmlZoomNodeBase extends Dynamic {

  type Type

  val actions: List[ZoomAction]

  def add(action: ZoomAction, actions: ZoomAction*): Type =
    addAll((action +: actions).toList)

  def addAll(action: List[ZoomAction]): Type

  def bind(ns: NodeSeq): BindedXmlZoom

  def unbind(): XmlZoom

  // ============================== ACTIONS ==============================
  def down(nodeName: String): Type =
    this.add(Down(nodeName))

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

  // ============================== DYNAMIC ==============================
  def applyDynamic(nodeName: String)(idx: Int): Type =
    this.addAll(List(Down(nodeName), AtIndex(idx)))

  def selectDynamic(nodeName: String): Type =
    this.add(Down(nodeName))
}

sealed trait XmlZoom extends XmlZoomNodeBase {

  override type Type = XmlZoom

  final def run[F[_]: ApplicativeThrowOrEu](document: NodeSeq): F[NodeSeq] =
    bind(document).run

  final def detailed[F[_]: ApplicativeThrowOrEu](document: NodeSeq): F[XmlZoomResult] =
    bind(document).detailed
}

sealed trait BindedXmlZoom extends XmlZoomNodeBase {

  override type Type = BindedXmlZoom

  val document: NodeSeq

  final def run[F[_]: ApplicativeThrowOrEu]: F[NodeSeq] =
    Functor[F].map(detailed)(_.nodeSeq)

  def detailed[F[_]: ApplicativeThrowOrEu]: F[XmlZoomResult]
}

sealed trait XmlZoomResult {
  val nodeSeq: NodeSeq
  val parents: List[NodeSeq]
}

/** [[XmlZoom]] is a powerful system that allow to "zoom" inside a `NodeSeq` and select one or more
  * elements keeping all step list and return a monadic value to handle possible errors.
  *
  * <h4>HOW TO USE</h4> [[XmlZoom]] is based on three types:
  *   - [[XmlZoom]] a.k.a XmlZoomUnbinded
  *   - [[BindedXmlZoom]]
  *   - [[XmlZoomResult]]
  *
  * <b>XmlZoom</b> Is the representation of unbind zoom instance. It contains only the list of the
  * actions to run on a `NodeSeq`.
  *
  * <b>BindedXmlZoom</b> Is the representation of binded zoom instance. Binded because it contains
  * both [[ZoomAction]] and `NodeSeq` target.
  *
  * <b>XmlZoomResult</b> Is the result of the [[XmlZoom]], that contains selected `NodeSeq` and his
  * parents.
  */
object XmlZoom {

  /** Just an alias for [[XmlZoom]], to use when you are building and XmlZoom that starts from the
    * root.
    */
  lazy val root: XmlZoom = XmlZoom.empty

  /** Just an alias for Root, to use when you are building and XmlZoom that not starts from the root
    * for the document. It's exists just to clarify the code. If your [[XmlZoom]] starts for the
    * root of the document please use `root`
    */
  lazy val $ : XmlZoom = XmlZoom.empty

  /** Just a binded alias for [[XmlZoom]], to use when you are building and XmlZoom that starts from
    * the root.
    */
  def root(document: NodeSeq): BindedXmlZoom = root.bind(document)

  /** Just a binded alias for root, to use when you are building and XmlZoom that not starts from
    * the root for the document. It's exists just to clarify the code. If your [[XmlZoom]] starts
    * for the root of the document please use `root`
    */
  def $(document: NodeSeq): BindedXmlZoom = $.bind(document)

  /** Empty unbinded [[XmlZoom]] instance, without any ZoomAction
    */
  lazy val empty: XmlZoom = XmlZoom(Nil)

  /** Create a new unbinded [[XmlZoom]] with specified actions.
    *
    * @param actions
    *   actions list for the zooming action
    * @return
    *   new instance of unbinded [[XmlZoom]]
    */
  def apply(actions: List[ZoomAction]): XmlZoom = Impls.Unbinded(actions)

  // ============================== IMPLS ==============================
  private object Impls {

    case class Unbinded(actions: List[ZoomAction]) extends XmlZoom {
      $thisZoom =>

      override def addAll(that: List[ZoomAction]): Type = copy(actions = actions ++ that)

      override def bind(ns: NodeSeq): BindedXmlZoom = Binded(ns, actions)

      override def unbind(): XmlZoom = this
    }

    case class Binded(document: NodeSeq, actions: List[ZoomAction]) extends BindedXmlZoom {
      $thisZoom =>

      override def addAll(that: List[ZoomAction]): Type = copy(actions = actions ++ that)

      override def bind(that: NodeSeq): Type = copy(document = that)

      override def unbind(): XmlZoom = Unbinded(actions)

      def detailed[F[_]](implicit F: ApplicativeThrowOrEu[F]): F[XmlZoomResult] = {

        @scala.annotation.tailrec
        def rec(
          current: XmlZoomResult,
          zActions: List[ZoomAction],
          logPath: String
        ): F[XmlZoomResult] = {
          zActions.headOption match {
            case None => F.pure(current)
            case Some(action) =>
              val newParents = action match {
                case Down(_) => current.parents :+ current.nodeSeq
                case _       => current.parents
              }

              action(current.nodeSeq) match {
                case Some(value) =>
                  rec(Impls.Result(value, newParents), zActions.tail, logPath + action.symbol)
                case None =>
                  F.raiseErrorOrEmpty(error.ZoomFailedException($thisZoom, action, logPath))
              }
          }
        }

        rec(Impls.Result(document, Nil), this.actions, "root")
      }
    }

    case class Result(nodeSeq: NodeSeq, parents: List[NodeSeq]) extends XmlZoomResult
  }

  // ============================== NODES ACTIONS ==============================
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
  object ZoomAction {

    def asStringPath(ls: List[ZoomAction]): String = {

      @tailrec
      def rec(tail: List[ZoomAction], acc: String = ""): String =
        tail match {
          case Nil          => acc
          case ::(head, tl) => rec(tl, acc + head.symbol)
        }

      rec(ls)
    }
  }

  sealed trait FilterZoomAction extends ZoomAction

  final case class Down(value: String) extends ZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns \ value)
    val symbol: String                      = s"/$value"
  }

  final case class Filter(p: XmlPredicate) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = checkEmpty(ns.filter(p))
    val symbol: String                      = s"| $p"
  }

  final case class Find(p: XmlPredicate) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.find(p)
    val symbol: String                      = s"find($p)"
  }

  final case class AtIndex(idx: Int) extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lift(idx)
    val symbol: String                      = s"($idx)"
  }

  case object Head extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.headOption
    val symbol: String                      = s"head()"
  }

  case object Last extends FilterZoomAction {
    def apply(ns: NodeSeq): Option[NodeSeq] = ns.lastOption
    val symbol: String                      = s"last()"
  }
}

case class XmlContentZoomRunner(zoom: BindedXmlZoom, f: NodeSeq => Value)
    extends AsValidable[XmlContentZoomRunner] {

  import cats.syntax.flatMap._

  def validate(nrule: ValidationRule, nrules: ValidationRule*): XmlContentZoomRunner =
    copy(f = f.andThen((v: Value) => v.validate(nrule, nrules: _*)))

  def validated: ValidatedNelThrow[String] =
    zoom.run[ValidatedNelThrow].andThen(ns => f(ns).extract[ValidatedNelThrow])

  def extract[F[_]: ApplicativeThrowOrEu: FlatMap]: F[String] =
    zoom.run[F].flatMap(ns => f(ns).extract[F])
}

object XmlContentZoom {

  // =========================== FROM NODESEQ ===========================
  def label(ns: NodeSeq): SimpleValue =
    ns match {
      case node: Node => SimpleValue(node.label)
      case _          => SimpleValue("")
    }

  def attr(ns: NodeSeq, key: String): ValidatedValue =
    SimpleValue(ns \@ key, Some(s"${label(ns).get} /@ $key")).nonEmpty

  def content(ns: NodeSeq): ValidatedValue =
    SimpleValue(ns.text, Some(s"${label(ns).get}.content")).nonEmpty

  // ========================= FROM BINDED ZOOM =========================
  def labelFromBindedZoom(zoom: BindedXmlZoom): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => label(ns))

  def attrFromBindedZoom(zoom: BindedXmlZoom, key: String): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => attr(ns, key))

  def contentFromBindedZoom(zoom: BindedXmlZoom): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => content(ns))

  // ========================= FROM UNBINDED ZOOM =========================
  def labelFromZoom(zoom: XmlZoom, ns: NodeSeq): XmlContentZoomRunner =
    labelFromBindedZoom(zoom.bind(ns))

  def attrFromZoom(zoom: XmlZoom, ns: NodeSeq, key: String): XmlContentZoomRunner =
    attrFromBindedZoom(zoom.bind(ns), key)

  def contentFromZoom(zoom: XmlZoom, ns: NodeSeq): XmlContentZoomRunner =
    contentFromBindedZoom(zoom.bind(ns))
}
