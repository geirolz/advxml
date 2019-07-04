package com.github.geirolz.advxml.convert

import cats.Semigroup
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

  def toTry[T](validated: ValidationRes[T])(implicit s: Semigroup[Throwable],
                                            manifest: reflect.Manifest[T]): Try[T] = {

    validated.fold(errors => {
      val className = manifest.runtimeClass.getName
      val errorsStr = errors.reduce

      Failure(new RuntimeException(s"Error validating model[$className] with errors: $errorsStr"))
    }, Success(_))
  }

  object ops extends ValidationSyntax

  object instances extends ValidationInstances
}


private [advxml] trait ValidationInstances {

  implicit val throwableMsgSemigroup : Semigroup[Throwable] = (x: Throwable, y: Throwable) =>
    new RuntimeException(x.getMessage + ",\n" + y.getMessage)

}

private[advxml] trait ValidationSyntax
  extends ValidatedSyntax
    with TupleSemigroupalSyntax
    with EitherSyntax {

  implicit class ValidationOps[T](validated: ValidationRes[T]) {
    def toTry(implicit s: Semigroup[Throwable], manifest: reflect.Manifest[T]): Try[T] = Validation.toTry(validated)
  }

  implicit class TryOps[T](t: Try[T]) {
    def toValidatedNel: ValidationRes[T] = Validation.fromTry(t)
  }

}