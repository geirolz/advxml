package advxml.core

import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}
import cats.Alternative

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/** Advxml
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
