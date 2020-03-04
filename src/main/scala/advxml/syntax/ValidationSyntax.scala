package advxml.syntax

import advxml.core.validate._
import cats.Alternative

import scala.util.Try

private[syntax] trait ValidationSyntax {

  implicit class ValidatedExTryOps[A](t: Try[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromTry(t)
  }

  implicit class ValidatedExEitherOps[A](e: EitherEx[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromEither(e)
  }

  implicit class ValidatedExEitherNelOps[A](e: EitherNelEx[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromEitherNel(e)
  }

  implicit class ValidatedExOptionOps[A](e: Option[A]) {
    def toValidatedEx(ifNone: => Throwable): ValidatedNelEx[A] = ValidatedNelEx.fromOption(e, ifNone)
  }

  implicit class ValidatedExOps[A](validated: ValidatedNelEx[A]) {

    def transformE[F[_]](implicit F: MonadEx[F]): F[A] =
      ValidatedNelEx.transformE[F, A](validated)(F)

    def transformNE[F[_]](implicit F: MonadNelEx[F]): F[A] =
      ValidatedNelEx.transformNE[F, A](validated)(F)

    def transformA[F[_]](implicit F: Alternative[F]): F[A] =
      ValidatedNelEx.transformA[F, A](validated)(F)
  }
}
