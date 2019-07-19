package com.github.geirolz.advxml.convert

import cats.Monad
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.exceptions.AggregatedException

import scala.util.{Failure, Success, Try}

object ValidatedRes {

  type ValidatedRes[T] = ValidatedNel[Throwable, T]

  def fromTry[T](t: Try[T]): ValidatedRes[T] = {
    import cats.implicits._
    t.toEither.toValidatedNel
  }

  def toTry[T](validated: ValidatedRes[T]): Try[T] = {
    validated match {
      case Valid(value) => Success(value)
      case Invalid(exs) => Failure(new AggregatedException("Multiple exceptions:\n", exs.toList))
    }
  }
}

private[advxml] trait ValidationInstances {

  implicit def validatedResMonad: Monad[ValidatedRes] = new Monad[ValidatedRes] {

    import cats.syntax.validated._

    override def flatMap[A, B](fa: ValidatedRes[A])(f: A => ValidatedRes[B]): ValidatedRes[B] =
      fa match {
        case Valid(a)       => f(a)
        case i @ Invalid(_) => i
      }

    override def tailRecM[A, B](a: A)(f: A => ValidatedRes[Either[A, B]]): ValidatedRes[B] =
      f(a) match {
        case Valid(vValue) =>
          vValue match {
            case Left(e)       => e.asInstanceOf[Throwable].invalidNel
            case Right(eValue) => eValue.validNel
          }
        case Invalid(e) => e.invalid
      }

    override def pure[A](x: A): ValidatedRes[A] = x.validNel
  }
}

private[advxml] trait ValidationSyntax {

  implicit class ValidatedResTryOps[T](t: Try[T]) {
    def toValidatedNel: ValidatedRes[T] = ValidatedRes.fromTry(t)
  }

  implicit class ValidatedResOps[T](validated: ValidatedRes[T]) {
    def toTry: Try[T] = ValidatedRes.toTry(validated)
  }

  implicit class ValidatedResOptionOps[T](t: ValidatedRes[Option[T]]) {
    def mapValue[A](f: T => A): ValidatedRes[Option[A]] = t.map(_.map(f))
  }
}
