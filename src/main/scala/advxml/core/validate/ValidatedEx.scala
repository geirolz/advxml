package advxml.core.validate

import advxml.core.validate.exceptions.AggregatedException
import cats.data.Validated.{Invalid, Valid}
import cats.Alternative
import cats.data.{NonEmptyList, Validated}

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
