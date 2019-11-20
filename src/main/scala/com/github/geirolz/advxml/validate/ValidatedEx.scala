package com.github.geirolz.advxml.validate

import cats.data.Validated.{Invalid, Valid}
import cats.{Alternative, MonadError}
import cats.data.{NonEmptyList, Validated}
import com.github.geirolz.advxml.validate.exceptions.AggregatedException

import scala.util.Try

object ValidatedEx {

  def fromTry[A](t: Try[A]): ValidatedEx[A] =
    t.fold(e => Invalid(NonEmptyList.of(e)), Valid(_))

  def fromEither[A](e: EitherEx[A]): ValidatedEx[A] =
    fromEitherNel(e.left.map(NonEmptyList.one))

  def fromEitherNel[A](e: EitherNelEx[A]): ValidatedEx[A] =
    Validated.fromEither(e)

  def fromOption[A](o: Option[A], ifNone: => Throwable): ValidatedEx[A] =
    Validated.fromOption(o, NonEmptyList.one(ifNone))

  def transformE[F[_], A](validated: ValidatedEx[A])(implicit F: MonadEx[F]): F[A] = {
    validated match {
      case Valid(value) => F.pure(value)
      case Invalid(exs) => F.raiseError(new AggregatedException(exs.toList))
    }
  }

  def transformNE[F[_], A](validated: ValidatedEx[A])(implicit F: MonadNelEx[F]): F[A] = {
    validated match {
      case Valid(value) => F.pure(value)
      case Invalid(exs) => F.raiseError(exs)
    }
  }

  def transformA[F[_], A](validated: ValidatedEx[A])(implicit F: Alternative[F]): F[A] = {
    validated match {
      case Valid(a)   => F.pure(a)
      case Invalid(_) => F.empty
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

  implicit def monadNelExLeftDsj[F[_]](implicit F: MonadNelEx[F]): MonadNelEx[F] \/ MonadEx[F] = Left(F)
  implicit def monadExRightDsj[F[_]](implicit F: MonadEx[F]): MonadNelEx[F] \/ MonadEx[F] = Right(F)

  implicit class ValidatedExTryOps[A](t: Try[A]) {
    def toValidatedEx: ValidatedEx[A] = ValidatedEx.fromTry(t)
  }

  implicit class ValidatedExEitherOps[A](e: EitherEx[A]) {
    def toValidatedEx: ValidatedEx[A] = ValidatedEx.fromEither(e)
  }

  implicit class ValidatedExEitherNelOps[A](e: EitherNelEx[A]) {
    def toValidatedEx: ValidatedEx[A] = ValidatedEx.fromEitherNel(e)
  }

  implicit class ValidatedExOptionOps[A](e: Option[A]) {
    def toValidatedEx(ifNone: => Throwable): ValidatedEx[A] = ValidatedEx.fromOption(e, ifNone)
  }

  implicit class ValidatedExOps[A](validated: ValidatedEx[A]) {

    def transformE[F[_]](implicit F: MonadEx[F]): F[A] =
      ValidatedEx.transformE[F, A](validated)(F)

    def transformNE[F[_]](implicit F: MonadNelEx[F]): F[A] =
      ValidatedEx.transformNE[F, A](validated)(F)

    def transformA[F[_]](implicit F: Alternative[F]): F[A] =
      ValidatedEx.transformA[F, A](validated)(F)
  }
}
