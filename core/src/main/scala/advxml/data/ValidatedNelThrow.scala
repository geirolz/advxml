package advxml.data

import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

import scala.util.Try

object ValidatedNelThrow {

  def fromTry[A](t: Try[A]): ValidatedNelThrow[A] =
    t.fold(e => Invalid(NonEmptyList.of(e)), Valid(_))

  def fromEither[A](e: EitherThrow[A]): ValidatedNelThrow[A] =
    fromEitherNel(e.left.map(NonEmptyList.one))

  def fromEitherNel[A](e: EitherNelThrow[A]): ValidatedNelThrow[A] =
    Validated.fromEither(e)

  def fromOption[A](o: Option[A], ifNone: => Throwable): ValidatedNelThrow[A] =
    Validated.fromOption(o, NonEmptyList.one(ifNone))
}
