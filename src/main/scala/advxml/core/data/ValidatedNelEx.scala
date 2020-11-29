package advxml.core.data

import advxml.core.data.error.AggregatedException
import advxml.core.{ErrorHandler, ExHandler}
import cats.data.Validated.{Invalid, Valid}
import cats.Monad
import cats.data.{NonEmptyList, Validated}

import scala.util.Try

object ValidatedNelEx {

  def fromTry[A](t: Try[A]): ValidatedNelEx[A] =
    t.fold(e => Invalid(NonEmptyList.of(e)), Valid(_))

  def fromEither[A](e: EitherEx[A]): ValidatedNelEx[A] =
    fromEitherNel(e.left.map(NonEmptyList.one))

  def fromEitherNel[A](e: EitherNelEx[A]): ValidatedNelEx[A] =
    Validated.fromEither(e)

  def fromOption[A](o: Option[A], ifNone: => Throwable): ValidatedNelEx[A] =
    Validated.fromOption(o, NonEmptyList.one(ifNone))

  def transform[F[_]: Monad: ExHandler, A](validated: ValidatedNelEx[A]): F[A] =
    ErrorHandler.fromValidated[F, Throwable, A](validated.leftMap(new AggregatedException(_)))
}
