package com.github.geirolz.advxml.convert

import cats.Semigroup
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.syntax.{EitherSyntax, TupleSemigroupalSyntax, ValidatedSyntax}
import com.github.geirolz.advxml.convert.Validation.ValidationRes

import scala.util.{Failure, Success, Try}


object Validation {

  type ValidationRes[T] = ValidatedNel[Throwable, T]

  def fromTry[T](t: Try[T]): ValidationRes[T] = {
    import cats.implicits._
    t.toEither.toValidatedNel
  }

  def toTry[T](validated: ValidationRes[T], headerMsg: Class[_] => String = _ => "")
              (implicit s: Semigroup[Throwable], manifest: reflect.Manifest[T]): Try[T] = {

    validated match {
      case Valid(value) => Success(value)
      case Invalid(exs) =>
        val clazz = manifest.runtimeClass
        val errMsg = exs.reduce
        Failure(
        new RuntimeException(s"${headerMsg(clazz)} \n $errMsg")
      )
    }
  }
}


private [advxml] trait ValidationInstances {

  implicit val throwableMsgSemigroup : Semigroup[Throwable] = (x: Throwable, y: Throwable) =>
    new RuntimeException(x.getMessage + ",\n" + y.getMessage)

}

private[advxml] trait ValidationSyntax
  extends ValidatedSyntax
    with TupleSemigroupalSyntax
    with EitherSyntax {

  implicit class ValidationTryOps[T](t: Try[T]) {
    def toValidatedNel: ValidationRes[T] = Validation.fromTry(t)
  }

  implicit class ValidationOps[T](validated: ValidationRes[T]) {
    def toTry(implicit s: Semigroup[Throwable], manifest: reflect.Manifest[T]): Try[T] = Validation.toTry(validated)
  }

  implicit class ValidationNestedOptionOps[T](t: ValidationRes[Option[T]]) {
    def toFlatOption: Option[T] = t.toOption.flatten
    def mapValue[A](f: T => A) : ValidationRes[Option[A]] = t.map(_.map(f))
  }
}