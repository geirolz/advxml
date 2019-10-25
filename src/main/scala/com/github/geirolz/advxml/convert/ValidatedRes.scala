package com.github.geirolz.advxml.convert

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.exceptions.AggregatedException

import scala.util.{Failure, Success, Try}

object ValidatedRes {

  type ValidatedRes[+T] = ValidatedNel[Throwable, T]

  def fromTry[T](t: Try[T]): ValidatedRes[T] = {
    import cats.implicits._
    t.toEither.toValidatedNel
  }

  def toTry[T](validated: ValidatedRes[T]): Try[T] = {
    validated match {
      case Valid(value) => Success(value)
      case Invalid(exs) => Failure(new AggregatedException(exs.toList))
    }
  }
}

private[advxml] trait ValidationSyntax {

  implicit class ValidatedResTryOps[T](t: Try[T]) {
    def toValidatedNel: ValidatedRes[T] = ValidatedRes.fromTry(t)
  }

  implicit class ValidatedResOps[T](validated: ValidatedRes[T]) {
    def toTry: Try[T] = ValidatedRes.toTry(validated)
  }
}
