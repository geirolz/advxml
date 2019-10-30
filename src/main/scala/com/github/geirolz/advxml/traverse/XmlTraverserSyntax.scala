package com.github.geirolz.advxml.traverse

import cats.{~>, Id, Monad}
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import com.github.geirolz.advxml.error.{MonadEx, ValidatedEx}

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTraverserSyntax extends XmlTraverserAbstractSyntax {

  object try_ {

    import cats.instances.option._
    import cats.instances.try_._

    implicit private[traverse] val optionToTryConverter: Option ~> Try = λ[Option ~> Try] {
      case Some(value) => Success(value)
      case None        => Failure(new RuntimeException("Missing XML element."))
    }

    implicit private[traverse] val idToTryConverter: Id ~> Try = λ[Id ~> Try](Success(_))

    implicit class XmlTraverseTryOps(target: NodeSeq) extends XmlTraverseTryIdOps(Success(target))

    implicit class XmlTraverseTryIdOps(target: Try[Id[NodeSeq]]) extends XmlTraverseMonadExOps[Try, Id](target)

    implicit class XmlTraverseTryOptionOps(target: Try[Option[NodeSeq]])
        extends XmlTraverseMonadExOps[Try, Option](target)
  }

  object either {

    import cats.instances.either._
    import cats.instances.option._

    implicit private[traverse] val optionToEitherConverter: Option ~> Either[Throwable, *] =
      λ[Option ~> Either[Throwable, *]] {
        case Some(value) => Right(value)
        case None        => Left(new RuntimeException("Missing XML element."))
      }

    implicit private[traverse] val idToEitherConverter: Id ~> Either[Throwable, *] =
      λ[Id ~> Either[Throwable, *]](Right(_))

    implicit class XmlTraverseEitherOps(target: NodeSeq) extends XmlTraverseEitherIdOps(Right(target))

    implicit class XmlTraverseEitherIdOps(target: Either[Throwable, Id[NodeSeq]])
        extends XmlTraverseMonadExOps[Either[Throwable, *], Id](target)

    implicit class XmlTraverseEitherOptionOps(target: Either[Throwable, Option[NodeSeq]])
        extends XmlTraverseMonadExOps[Either[Throwable, *], Option](target)
  }

  object validated {

    import cats.instances.option._
    import com.github.geirolz.advxml.instances.validation._

    implicit private[traverse] val idToValidatedExConverter: Id ~> ValidatedEx =
      λ[Id ~> ValidatedEx](Valid(_))

    implicit private[traverse] val optionToValidatedExConverter: Option ~> ValidatedEx =
      λ[Option ~> ValidatedEx] {
        case Some(value) => Valid(value)
        case None        => Invalid(NonEmptyList.of(new RuntimeException("Missing XML element.")))
      }

    implicit class XmlTraverseValidatedExOps(target: NodeSeq) extends XmlTraverseValidatedExIdOps(Valid(target))

    implicit class XmlTraverseValidatedExIdOps(target: ValidatedEx[Id[NodeSeq]])
        extends XmlTraverseMonadExOps[ValidatedEx, Id](target)

    implicit class XmlTraverseValidatedExOptionOps(target: ValidatedEx[Option[NodeSeq]])
        extends XmlTraverseMonadExOps[ValidatedEx, Option](target)
  }
}

private[advxml] trait XmlTraverserAbstractSyntax {

  implicit def idConverter[A[_]]: A ~> A = λ[A ~> A](a => a)

  implicit private[traverse] val idToOptionConverter: Id ~> Option = λ[Id ~> Option](Some(_))

  implicit class XmlTraverseNodeSeqOps(target: NodeSeq) {

    def \![F[_]: MonadEx](q: String): F[Id[NodeSeq]] =
      XmlTraverser.mandatory.immediateChildren(target, q)

    def \\![F[_]: MonadEx](q: String): F[Id[NodeSeq]] =
      XmlTraverser.mandatory.children(target, q)

    def \@![F[_]: MonadEx](q: String): F[Id[String]] =
      XmlTraverser.mandatory.attr(target, q)

    def ![F[_]: MonadEx]: F[Id[String]] =
      XmlTraverser.mandatory.text(target)

    def \?[F[_]: MonadEx](q: String): F[Option[NodeSeq]] =
      XmlTraverser.optional.immediateChildren(target, q)

    def \\?[F[_]: MonadEx](q: String): F[Option[NodeSeq]] =
      XmlTraverser.optional.children(target, q)

    def \@?[F[_]: MonadEx](q: String): F[Option[String]] =
      XmlTraverser.optional.attr(target, q)

    def ?[F[_]: MonadEx]: F[Option[String]] =
      XmlTraverser.optional.text(target)
  }

  implicit class XmlTraverseMonadExOps[F[_]: MonadEx, G[_]: Monad](fg: F[G[NodeSeq]])(
    implicit C: G ~> F,
    O: G ~> Option
  ) {

    import cats.implicits._

    def \!(q: String): F[Id[NodeSeq]] = mandatory(_.\![F](q))

    def \\!(q: String): F[Id[NodeSeq]] = mandatory(_.\\![F](q))

    def \@!(q: String): F[Id[String]] = mandatory(_.\@![F](q))

    def ! : F[Id[String]] = mandatory(_.![F])

    def \?(q: String): F[Option[NodeSeq]] = optional(_.\?[Try](q))

    def \\?(q: String): F[Option[NodeSeq]] = optional(_.\\?[Try](q))

    def \@?(q: String): F[Option[String]] = optional(_.\@?[Try](q))

    def ? : F[Option[String]] = optional(_.?[Try])

    private def mandatory[T](op: NodeSeq => F[T]): F[T] =
      fg.flatMap(C.apply).flatMap(op)

    private def optional[T](op: NodeSeq => Try[Option[T]]): F[Option[T]] =
      fg.map(O.apply(_).map(op).flatMap {
        case Failure(_)     => None
        case Success(value) => value
      })
  }
}
