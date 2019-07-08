package com.github.geirolz.advxml.convert

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.{Monad, Semigroup}
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

import scala.util.{Failure, Success, Try}


object ValidatedRes {

  type ValidatedRes[T] = ValidatedNel[Throwable, T]

  def fromTry[T](t: Try[T]): ValidatedRes[T] = {
    import cats.implicits._
    t.toEither.toValidatedNel
  }

  def toTry[T](validated: ValidatedRes[T])(implicit s: Semigroup[Throwable]): Try[T] = {
    validated match {
      case Valid(value) => Success(value)
      case Invalid(exs) => Failure(new RuntimeException(exs.reduce))
    }
  }
}


private [advxml] trait ValidationInstances {

  implicit val throwableMsgSemigroup : Semigroup[Throwable] = (x: Throwable, y: Throwable) =>
    new RuntimeException(x.getMessage + ",\n" + y.getMessage)

  implicit def validatedResMonad: Monad[ValidatedRes] = new Monad[ValidatedRes] {

    import cats.syntax.validated._

    override def flatMap[A, B](fa: ValidatedRes[A])(f: A => ValidatedRes[B]): ValidatedRes[B] =
      fa match {
        case Valid(a) => f(a)
        case i @ Invalid(_) => i
      }

    override def tailRecM[A, B](a: A)(f: A => ValidatedRes[Either[A, B]]): ValidatedRes[B] =
      f(a) match {
        case Valid(vValue) => vValue match {
          case Left(e) => e.asInstanceOf[Throwable].invalidNel
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
    def toTry(implicit s: Semigroup[Throwable]): Try[T] = ValidatedRes.toTry(validated)
  }

  implicit class ValidatedResOptionOps[T](t: ValidatedRes[Option[T]]) {
    def flattenOption: Option[T] = t.toOption.flatten
    def mapValue[A](f: T => A) : ValidatedRes[Option[A]] = t.map(_.map(f))
  }
}