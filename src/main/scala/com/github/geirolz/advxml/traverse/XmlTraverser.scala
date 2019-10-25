package com.github.geirolz.advxml.traverse

import cats.Alternative
import com.github.geirolz.advxml.transform.actions.MonadEx
import com.github.geirolz.advxml.traverse.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
sealed trait XmlTraverser[F[_]] {
  def immediateChildren(target: NodeSeq, q: String): F[NodeSeq]

  def children(target: NodeSeq, q: String): F[NodeSeq]

  def attr(target: NodeSeq, q: String): F[String]

  def text(target: NodeSeq): F[String]
}
object XmlTraverser {

  def mandatory[F[_]](implicit F: MonadEx[F]): XmlTraverser[F] = new XmlTraverser[F] {

    def immediateChildren(target: NodeSeq, q: String): F[NodeSeq] = {
      target \ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingNodeException(q, target))
        case value                  => F.pure(value)
      }
    }

    def children(target: NodeSeq, q: String): F[NodeSeq] = {
      target \\ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingNodeException(q, target))
        case value                  => F.pure(value)
      }
    }

    def attr(target: NodeSeq, q: String): F[String] = {
      target \@ q match {
        case value if value.isEmpty => F.raiseError(XmlMissingAttributeException(q, target))
        case value                  => F.pure(value)
      }
    }

    def text(target: NodeSeq): F[String] = {
      target.text match {
        case value if value.isEmpty => F.raiseError(XmlMissingTextException(target))
        case value                  => F.pure(value)
      }
    }
  }

  def optional[F[_]](implicit F: Alternative[F]): XmlTraverser[F] = new XmlTraverser[F] {

    import cats.instances.try_._

    def immediateChildren(ns: NodeSeq, q: String): F[NodeSeq] =
      toAlternative(mandatory.immediateChildren(ns, q))

    def children(ns: NodeSeq, q: String): F[NodeSeq] =
      toAlternative(mandatory.children(ns, q))

    def attr(ns: NodeSeq, q: String): F[String] =
      toAlternative(mandatory.attr(ns, q))

    def text(ns: NodeSeq): F[String] =
      toAlternative(mandatory.text(ns))

    private def toAlternative[T](v: Try[T]): F[T] = v match {
      case Success(value) => F.pure(value)
      case Failure(_)     => F.empty
    }
  }

  object exceptions {

    abstract class XmlMissingException(val message: String) extends RuntimeException(message) {
      val target: NodeSeq
    }

    case class XmlMissingNodeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for node: $q.")

    case class XmlMissingAttributeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for attribute: $q.")

    case class XmlMissingTextException(target: NodeSeq) extends XmlMissingException(s"Missing text, content is empty.")

  }
}
