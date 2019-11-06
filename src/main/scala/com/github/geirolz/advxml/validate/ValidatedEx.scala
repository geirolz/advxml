package com.github.geirolz.advxml.validate

import cats.data.Validated.{Invalid, Valid}
import cats.MonadError
import cats.data.{NonEmptyList, Validated}
import com.github.geirolz.advxml.validate.exceptions.AggregatedException

import scala.util.Try

object ValidatedEx {

  def fromTry[A](t: Try[A]): ValidatedEx[A] =
    t.fold(e => Invalid(NonEmptyList.of(e)), Valid(_))

  def fromEither[A](e: EitherNelEx[A]): ValidatedEx[A] =
    Validated.fromEither(e)

  def fromOption[A](o: Option[A], ifNone: => ThrowableNel): ValidatedEx[A] =
    Validated.fromOption(o, ifNone)

  def transform[F[_], A](validated: ValidatedEx[A])(implicit F: MonadEx[F] \/ MonadNelEx[F]): F[A] = {
    F match {
      case Left(m) =>
        validated match {
          case Valid(value) => m.pure(value)
          case Invalid(exs) => m.raiseError(new AggregatedException(exs.toList))
        }
      case Right(m) =>
        validated match {
          case Valid(value) => m.pure(value)
          case Invalid(exs) => m.raiseError(exs)
        }
    }
  }
}

private[advxml] trait ValidationInstance {

  implicit val validatedNelMonadErrorThrowableInstance: MonadEx[ValidatedEx] =
    validatedMonadErrorInstance[ThrowableNel, Throwable](
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

  implicit def monadExLeftDsj[F[_]](implicit F: MonadEx[F]): MonadEx[F] \/ MonadNelEx[F] = Left(F)
  implicit def monadNelExRightDsj[F[_]](implicit F: MonadNelEx[F]): MonadEx[F] \/ MonadNelEx[F] = Right(F)

  implicit def monadNelExLeftDsj[F[_]: MonadNelEx]: MonadNelEx[F] \/ MonadEx[F] = monadNelExRightDsj[F].swap
  implicit def monadExRightDsj[F[_]: MonadEx]: MonadNelEx[F] \/ MonadEx[F] = monadExLeftDsj[F].swap

  implicit class ValidatedExTryOps[A](t: Try[A]) {
    def toValidatedNel: ValidatedEx[A] = ValidatedEx.fromTry(t)
  }

  implicit class ValidatedExEitherOps[A](e: EitherEx[A]) {
    def toValidatedNel: ValidatedEx[A] =
      Validated.fromEither(e.left.map(NonEmptyList.of(_)))
  }

  implicit class ValidatedExEitherNelOps[A](e: EitherNelEx[A]) {
    def toValidatedNel: ValidatedEx[A] = ValidatedEx.fromEither(e)
  }

  implicit class ValidatedExOptionOps[A](e: Option[A]) {
    def toValidatedNel(ifNone: => ThrowableNel): ValidatedEx[A] = ValidatedEx.fromOption(e, ifNone)
  }

  implicit class ValidatedResOps[A](validated: ValidatedEx[A]) {

    def transform[F[_]](implicit F: MonadEx[F] \/ MonadNelEx[F]): F[A] =
      ValidatedEx.transform[F, A](validated)(F)
  }
}
