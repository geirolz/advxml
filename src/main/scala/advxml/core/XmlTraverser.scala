package advxml.core

import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}
import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import cats.{~>, Alternative, FlatMap}

import scala.language.dynamics
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
object XmlTraverser {

  import cats.syntax.functor._

  sealed trait XmlTraverser[F[_]] {
    def immediateChildren(target: NodeSeq, q: String): F[NodeSeq]

    def children(ns: NodeSeq, q: String): F[NodeSeq]

    def attr(ns: NodeSeq, q: String): F[String]

    def text(ns: NodeSeq): F[String]

    def trimmedText(ns: NodeSeq): F[String]
  }

  def mandatory[F[_]](implicit F: MonadEx[F]): XmlTraverser[F] = new XmlTraverser[F] {

    override def immediateChildren(ns: NodeSeq, q: String): F[NodeSeq] = {
      ns \ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingNodeException(q, ns))
        case value                  => F.pure(value)
      }
    }

    override def children(ns: NodeSeq, q: String): F[NodeSeq] = {
      ns \\ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingNodeException(q, ns))
        case value                  => F.pure(value)
      }
    }

    override def attr(ns: NodeSeq, q: String): F[String] = {
      ns \@ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingAttributeException(q, ns))
        case value                  => F.pure(value)
      }
    }

    override def text(ns: NodeSeq): F[String] = {
      ns.text match {
        case value if value.isEmpty => F.raiseError(XmlMissingTextException(ns))
        case value                  => F.pure(value)
      }
    }

    override def trimmedText(ns: NodeSeq): F[String] = text(ns).map(_.trim)
  }

  def optional[F[_]](implicit F: Alternative[F]): XmlTraverser[F] = new XmlTraverser[F] {

    import cats.instances.try_._

    override def immediateChildren(ns: NodeSeq, q: String): F[NodeSeq] =
      toAlternative(mandatory[Try].immediateChildren(ns, q))

    override def children(ns: NodeSeq, q: String): F[NodeSeq] =
      toAlternative(mandatory[Try].children(ns, q))

    override def attr(ns: NodeSeq, q: String): F[String] =
      toAlternative(mandatory[Try].attr(ns, q))

    override def text(ns: NodeSeq): F[String] =
      toAlternative(mandatory[Try].text(ns))

    private def toAlternative[T](v: Try[T]): F[T] = v match {
      case Failure(_)     => Alternative[F].empty
      case Success(value) => Alternative[F].pure(value)
    }

    override def trimmedText(ns: NodeSeq): F[String] = text(ns).map(_.trim)
  }

  object exceptions {

    abstract class XmlMissingException(val message: String) extends RuntimeException(message) {
      val target: NodeSeq
    }

    case class XmlMissingNodeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for node: $q")

    case class XmlMissingAttributeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for attribute: $q")

    case class XmlMissingTextException(target: NodeSeq) extends XmlMissingException(s"Missing text, content is empty")
  }
}

object XmlDynamicTraverser {

  sealed trait XmlDynamicTraverser[F[_]] extends Dynamic {

    def selectDynamic(q: String): XmlDynamicTraverser[F]

    def applyDynamic(q: String)(idx: Int): XmlDynamicTraverser[F]

    def atIndex(idx: Int): XmlDynamicTraverser[F]

    def head: XmlDynamicTraverser[F]

    def last: XmlDynamicTraverser[F]

    def filter(p: XmlPredicate): XmlDynamicTraverser[F]

    def map(f: NodeSeq => NodeSeq): XmlDynamicTraverser[F]

    def flatMap(f: NodeSeq => F[NodeSeq]): XmlDynamicTraverser[F]

    def get: F[NodeSeq]

    def attr(q: String): F[String]

    def text: F[String]

    def trimmedText: F[String]
  }

  private class XmlDynamicTraverserImp[F[_]: FlatMap](
    value: F[NodeSeq],
    optHandler: Throwable => Option ~> F,
    downAction: (NodeSeq, String) => F[NodeSeq]
  ) extends XmlDynamicTraverser[F] {

    import cats.syntax.all._

    def selectDynamic(q: String): XmlDynamicTraverser[F] =
      flatMap(v => downAction(v, q))

    def applyDynamic(q: String)(idx: Int): XmlDynamicTraverser[F] =
      selectDynamic(q).atIndex(idx)

    def atIndex(idx: Int): XmlDynamicTraverser[F] =
      flatMap(v => optHandler(new IndexOutOfBoundsException("" + idx))(v.lift(idx)))

    def head: XmlDynamicTraverser[F] =
      flatMap(v => optHandler(new NoSuchElementException())(v.headOption))

    def last: XmlDynamicTraverser[F] =
      flatMap(v => optHandler(new NoSuchElementException())(v.lastOption))

    def filter(p: XmlPredicate): XmlDynamicTraverser[F] =
      map(_.filter(p))

    def map(f: NodeSeq => NodeSeq): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp(value.map(f), optHandler, downAction)

    def flatMap(f: NodeSeq => F[NodeSeq]): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp(value.flatMap(f), optHandler, downAction)

    //exit points
    def get: F[NodeSeq] = value

    def attr(q: String): F[String] =
      for {
        node <- value
        optAttr <- value.map(n =>
          n \@ q match {
            case "" => None
            case x  => Some(x)
          }
        )
        result <- optHandler(XmlMissingAttributeException(q, node))(optAttr)
      } yield result

    def text: F[String] =
      value.flatMap(ns => {
        value
          .map(n =>
            n.text match {
              case x if x.isEmpty => None
              case x              => Some(x)
            }
          )
          .flatMap(textOpt => {
            optHandler(XmlMissingTextException(ns))(textOpt)
          })
      })

    def trimmedText: F[String] = text.map(_.trim)
  }

  object mandatory {

    private def optHandler[F[_]: MonadEx]: Throwable => Option ~> F =
      throwable =>
        λ[Option ~> F] {
          case Some(value) => MonadEx[F].pure(value)
          case None        => MonadEx[F].raiseError(throwable)
        }

    def immediate[F[_]: MonadEx](v: NodeSeq): XmlDynamicTraverser[F] =
      immediate(MonadEx[F].pure(v))

    def immediate[F[_]: MonadEx](v: F[NodeSeq]): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp[F](v, optHandler, XmlTraverser.mandatory[F].immediateChildren)

    def deep[F[_]: MonadEx](v: NodeSeq): XmlDynamicTraverser[F] =
      deep(MonadEx[F].pure(v))

    def deep[F[_]: MonadEx](v: F[NodeSeq]): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp[F](v, optHandler, XmlTraverser.mandatory[F].children)
  }

  object optional {

    private def optHandler[F[_]: Alternative]: Throwable => Option ~> F =
      _ =>
        λ[Option ~> F] {
          case Some(value) => Alternative[F].pure(value)
          case None        => Alternative[F].empty
        }

    def immediate[F[_]: Alternative: FlatMap](v: NodeSeq): XmlDynamicTraverser[F] =
      immediate(Alternative[F].pure(v))

    def immediate[F[_]: Alternative: FlatMap](v: F[NodeSeq]): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp[F](v, optHandler, XmlTraverser.optional[F].immediateChildren)

    def deep[F[_]: Alternative: FlatMap](v: NodeSeq): XmlDynamicTraverser[F] =
      deep(Alternative[F].pure(v))

    def deep[F[_]: Alternative: FlatMap](v: F[NodeSeq]): XmlDynamicTraverser[F] =
      new XmlDynamicTraverserImp[F](v, optHandler, XmlTraverser.optional[F].children)
  }
}
