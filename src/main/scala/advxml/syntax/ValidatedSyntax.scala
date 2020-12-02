package advxml.syntax

import advxml.core.data._
import advxml.core.ExHandler
import cats.Monad

import scala.util.Try

private[syntax] trait ValidatedSyntax {

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
    def transform[F[_]: Monad: ExHandler]: F[A] =
      ValidatedNelEx.transform[F, A](validated)
  }
}
