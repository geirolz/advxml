package com.github.geirolz.advxml.error

import cats.data.Validated.{Invalid, Valid}
import cats.MonadError
import cats.data.{NonEmptyList, Validated}
import com.github.geirolz.advxml.error.exceptions.AggregatedException

import scala.util.Try

object ValidatedEx {

  def fromTry[A](t: Try[A]): ValidatedEx[A] =
    t.fold(e => Invalid(NonEmptyList.of(e)), Valid(_))

  def fromEither[A](e: Either[NonEmptyList[Throwable], A]): ValidatedEx[A] =
    Validated.fromEither(e)

  def fromOption[A](o: Option[A], ifNone: => NonEmptyList[Throwable]): ValidatedEx[A] =
    Validated.fromOption(o, ifNone)

  def transformNel[F[_], A](validated: ValidatedEx[A])(implicit F: MonadError[F, NonEmptyList[Throwable]]): F[A] = {
    validated match {
      case Valid(value) => F.pure(value)
      case Invalid(exs) => F.raiseError(exs)
    }
  }

  def transform[F[_], A](validated: ValidatedEx[A])(implicit F: MonadEx[F]): F[A] = {
    validated match {
      case Valid(value) => F.pure(value)
      case Invalid(exs) => F.raiseError(new AggregatedException(exs.toList))
    }
  }
}

private[advxml] trait ValidationInstance {

  implicit val validatedNelMonadErrorThrowableInstance: MonadEx[ValidatedEx] =
    validatedMonadErrorInstance[NonEmptyList[Throwable], Throwable](
      NonEmptyList.of(_),
      nelE => new AggregatedException(nelE.toList)
    )

  implicit def validatedMonadErrorInstanceSameError[FE, ME](
    implicit C1: ME =:= FE,
    C2: FE =:= ME
  ): MonadError[Validated[FE, *], ME] =
    validatedMonadErrorInstance[FE, ME](C1.apply, C2.apply)

  implicit def validatedMonadErrorInstance[FE, ME](
    implicit toFe: ME => FE,
    toMe: FE => ME
  ): MonadError[Validated[FE, *], ME] =
    new MonadError[Validated[FE, *], ME] {

      def raiseError[A](e: ME): Validated[FE, A] = Invalid(e)

      def pure[A](x: A): Validated[FE, A] = Valid(x)

      def handleErrorWith[A](fa: Validated[FE, A])(f: ME => Validated[FE, A]): Validated[FE, A] =
        fa match {
          case Valid(a)   => Valid(a)
          case Invalid(e) => f(toMe(e))
        }

      def flatMap[A, B](fa: Validated[FE, A])(f: A => Validated[FE, B]): Validated[FE, B] =
        fa match {
          case Valid(a)   => f(a)
          case Invalid(e) => Invalid(e)
        }

      @scala.annotation.tailrec
      def tailRecM[A, B](a: A)(f: A => Validated[FE, Either[A, B]]): Validated[FE, B] =
        f(a) match {
          case Valid(eitherAb) =>
            eitherAb match {
              case Right(b) => Valid(b)
              case Left(a)  => tailRecM(a)(f)
            }
          case Invalid(e) => Invalid(e)
        }
    }
}

private[advxml] trait ValidationSyntax {

  implicit class ValidatedExTryOps[A](t: Try[A]) {
    def toValidatedNel: ValidatedEx[A] = ValidatedEx.fromTry(t)
  }

  implicit class ValidatedExEitherOps[A](e: Either[Throwable, A]) {
    def toValidatedNel: ValidatedEx[A] =
      Validated.fromEither(e.left.map(NonEmptyList.of(_)))
  }

  implicit class ValidatedExEitherNelOps[A](e: Either[NonEmptyList[Throwable], A]) {
    def toValidatedNel: ValidatedEx[A] = ValidatedEx.fromEither(e)
  }

  implicit class ValidatedExOptionOps[A](e: Option[A]) {
    def toValidatedNel(ifNone: => NonEmptyList[Throwable]): ValidatedEx[A] = ValidatedEx.fromOption(e, ifNone)
  }

  implicit class ValidatedResOps[A](validated: ValidatedEx[A]) {

    def transformNel[F[_]](implicit F: MonadError[F, NonEmptyList[Throwable]]): F[A] =
      ValidatedEx.transformNel[F, A](validated)

    def transform[F[_]](implicit F: MonadEx[F]): F[A] =
      ValidatedEx.transform[F, A](validated)
  }
}
