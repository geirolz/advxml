package com.github.geirolz.advxml.traverse

import cats.Alternative
import com.github.geirolz.advxml.transform.actions.MonadEx

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
sealed trait XmlTraverser[F[_]] {
  def immediateChildren(ns: NodeSeq, name: String): F[NodeSeq]

  def children(ns: NodeSeq, name: String): F[NodeSeq]

  def attr(ns: NodeSeq, name: String): F[String]

  def text(ns: NodeSeq): F[String]
}
object XmlTraverser {

  def mandatory[F[_]](implicit F: MonadEx[F]): XmlTraverser[F] = new XmlTraverser[F] {

    def immediateChildren(ns: NodeSeq, name: String): F[NodeSeq] = {
      ns \ name match {
        case value if value.isEmpty => F.raiseError(new RuntimeException(s"Missing node: $name"))
        case value                  => F.pure(value)
      }
    }

    def children(ns: NodeSeq, name: String): F[NodeSeq] = {
      ns \\ name match {
        case value if value.isEmpty => F.raiseError(new RuntimeException(s"Missing nested node: $name"))
        case value                  => F.pure(value)
      }
    }

    def attr(ns: NodeSeq, name: String): F[String] = {
      ns \@ name match {
        case value if value.isEmpty => F.raiseError(new RuntimeException(s"Missing attribute: $name"))
        case value                  => F.pure(value)
      }
    }

    def text(ns: NodeSeq): F[String] = {
      ns.text match {
        case value if value.isEmpty => F.raiseError(new RuntimeException("Missing text"))
        case value                  => F.pure(value)
      }
    }
  }

  def optional[F[_]](implicit F: Alternative[F]): XmlTraverser[F] = new XmlTraverser[F] {

    import cats.instances.try_._

    def immediateChildren(ns: NodeSeq, name: String): F[NodeSeq] =
      toAlternative(mandatory.immediateChildren(ns, name))

    def children(ns: NodeSeq, name: String): F[NodeSeq] =
      toAlternative(mandatory.children(ns, name))

    def attr(ns: NodeSeq, name: String): F[String] =
      toAlternative(mandatory.attr(ns, name))

    def text(ns: NodeSeq): F[String] =
      toAlternative(mandatory.text(ns))

    private def toAlternative[T](v: Try[T]): F[T] = v match {
      case Success(value) => F.pure(value)
      case Failure(_)     => F.empty
    }
  }
}
