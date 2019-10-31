package com.github.geirolz.advxml.traverse

import cats.Id
import com.github.geirolz.advxml.validate.MonadEx
import com.github.geirolz.advxml.traverse.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
sealed trait XmlTraverser[F[_], G[_]] {
  def immediateChildren(target: NodeSeq, q: String): F[G[NodeSeq]]

  def children(target: NodeSeq, q: String): F[G[NodeSeq]]

  def attr(target: NodeSeq, q: String): F[G[String]]

  def text(target: NodeSeq): F[G[String]]
}
object XmlTraverser {

  def mandatory[F[_]](implicit F: MonadEx[F]): XmlTraverser[F, Id] = new XmlTraverser[F, Id] {

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

  def optional[F[_]](implicit F: MonadEx[F]): XmlTraverser[F, Option] = new XmlTraverser[F, Option] {

    import cats.implicits._

    def immediateChildren(ns: NodeSeq, q: String): F[Option[NodeSeq]] =
      mandatory[F].immediateChildren(ns, q).map[Option[NodeSeq]](Some(_)).orElse(F.pure(None))

    def children(ns: NodeSeq, q: String): F[Option[NodeSeq]] =
      mandatory[F].children(ns, q).map[Option[NodeSeq]](Some(_)).orElse(F.pure(None))

    def attr(ns: NodeSeq, q: String): F[Option[String]] =
      mandatory[F].attr(ns, q).map[Option[String]](Some(_)).orElse(F.pure(None))

    def text(ns: NodeSeq): F[Option[String]] =
      mandatory[F].text(ns).map[Option[String]](Some(_)).orElse(F.pure(None))
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
