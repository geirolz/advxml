package advxml.syntax

import advxml.core.validate.{EitherEx, MonadEx, ValidatedEx}
import advxml.core.XmlTraverser
import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import cats.{~>, Id, Monad}
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[syntax] trait XmlTraverserAbstractSyntax {

  implicit def idConverter[A[_]]: A ~> A = λ[A ~> A](a => a)

  implicit private[syntax] val idToOptionConverter: Id ~> Option = λ[Id ~> Option](Some(_))

  class XmlTraverseSyntaxDsl[F[_]: MonadEx, G[_]: Monad](fg: F[G[NodeSeq]])(
    implicit G_to_F: G ~> F,
    G_to_Option: G ~> Option
  ) {

    import cats.implicits._

    def \!(q: String): F[NodeSeq] =
      mandatory(XmlTraverser.mandatory[F].immediateChildren(_, q))

    def \\!(q: String): F[NodeSeq] =
      mandatory(XmlTraverser.mandatory[F].children(_, q))

    def \@!(q: String): F[String] =
      mandatory(XmlTraverser.mandatory[F].attr(_, q))

    def ! : F[String] =
      mandatory(XmlTraverser.mandatory[F].text(_))

    def \?(q: String): F[Option[NodeSeq]] =
      optional(XmlTraverser.optional[Try].immediateChildren(_, q))

    def \\?(q: String): F[Option[NodeSeq]] =
      optional(XmlTraverser.optional[Try].children(_, q))

    def \@?(q: String): F[Option[String]] =
      optional(XmlTraverser.optional[Try].attr(_, q))

    def ? : F[Option[String]] =
      optional(XmlTraverser.optional[Try].text(_))

    private def mandatory[T](op: NodeSeq => F[T]): F[T] =
      fg.flatMap(G_to_F.apply).flatMap(op)

    private def optional[T](op: NodeSeq => Try[Option[T]]): F[Option[T]] =
      fg.map(G_to_Option.apply(_).map(op).flatMap {
        case Failure(_)     => None
        case Success(value) => value
      })
  }
}

private[syntax] trait XmlTraverserTrySyntax extends XmlTraverserAbstractSyntax {

  import cats.instances.option._
  import cats.instances.try_._

  implicit private[syntax] val idToTryConverter: Id ~> Try = λ[Id ~> Try](Success(_))

  implicit private[syntax] val optionToTryConverter: Option ~> Try = λ[Option ~> Try] {
    case Some(value) => Success(value)
    case None        => Failure(new RuntimeException("Missing XML element."))
  }

  implicit class XmlTraverseSyntaxDsl_Id_TryId(target: NodeSeq) extends XmlTraverseSyntaxDsl[Try, Id](Success(target))

  implicit class XmlTraverseSyntaxDsl_Try_TryId(target: Try[NodeSeq]) extends XmlTraverseSyntaxDsl[Try, Id](target)

  implicit class XmlTraverseSyntaxDsl_TryOption_TryOption(target: Try[Option[NodeSeq]])
      extends XmlTraverseSyntaxDsl[Try, Option](target)
}

private[syntax] trait XmlTraverserEitherSyntax extends XmlTraverserAbstractSyntax {
  import cats.instances.either._
  import cats.instances.option._

  implicit private[syntax] val idToEitherConverter: Id ~> EitherEx = λ[Id ~> EitherEx](Right(_))

  implicit private[syntax] val optionToEitherConverter: Option ~> EitherEx =
    λ[Option ~> EitherEx] {
      case Some(value) => Right(value)
      case None        => Left(new RuntimeException("Missing XML element."))
    }

  implicit class XmlTraverseSyntaxDsl_Id_EitherExId(target: NodeSeq)
      extends XmlTraverseSyntaxDsl[EitherEx, Id](Right(target))

  implicit class XmlTraverseSyntaxDsl_EitherEx_EitherExId(target: EitherEx[NodeSeq])
      extends XmlTraverseSyntaxDsl[EitherEx, Id](target)

  implicit class XmlTraverseSyntaxDsl_EitherExOption_EitherExOption(target: EitherEx[Option[NodeSeq]])
      extends XmlTraverseSyntaxDsl[EitherEx, Option](target)
}

private[syntax] trait XmlTraverserValidatedSyntax extends XmlTraverserAbstractSyntax {

  import cats.instances.option._
  import advxml.instances.validate._

  implicit private[syntax] val idToValidatedExConverter: Id ~> ValidatedEx = λ[Id ~> ValidatedEx](Valid(_))

  implicit private[syntax] val optionToValidatedExConverter: Option ~> ValidatedEx =
    λ[Option ~> ValidatedEx] {
      case Some(value) => Valid(value)
      case None        => Invalid(NonEmptyList.of(new RuntimeException("Missing XML element.")))
    }

  implicit class XmlTraverseSyntaxDsl_Id_ValidatedEx(target: NodeSeq)
      extends XmlTraverseSyntaxDsl[ValidatedEx, Id](Valid(target))

  implicit class XmlTraverseSyntaxDsl_ValidatedEx_ValidatedExId(target: ValidatedEx[NodeSeq])
      extends XmlTraverseSyntaxDsl[ValidatedEx, Id](target)

  implicit class XmlTraverseSyntaxDsl_ValidatedExOption_ValidatedExOption(target: ValidatedEx[Option[NodeSeq]])
      extends XmlTraverseSyntaxDsl[ValidatedEx, Option](target)
}
