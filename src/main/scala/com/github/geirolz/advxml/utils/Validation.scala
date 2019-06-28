package com.github.geirolz.advxml.utils

import cats.data.ValidatedNel
import cats.syntax.{EitherSyntax, TupleSemigroupalSyntax, ValidatedSyntax}
import com.github.geirolz.advxml.utils.Validation.ValidatedEx

import scala.util.{Failure, Success, Try}


sealed trait Validation
  extends ValidatedSyntax
    with TupleSemigroupalSyntax
    with EitherSyntax{


  implicit class ValidationTryOps[T](t: Try[T]) {
    def validate: ValidatedEx[T] = t.toEither.toValidatedNel
  }

  implicit class ValidatedUtils[T](validated: ValidatedEx[T]) {
    def toTry(implicit manifest: reflect.Manifest[T]): Try[T] = {
      validated.fold(errors => {
        val className = manifest.runtimeClass.getName
        val errorsStr = errors
          .toList
          .map(_.getMessage)
          .reduce((e1, e2) => e1 + ",\n" + e2)

        Failure(new RuntimeException(s"Error validating model[$className] with errors: $errorsStr"))
      }, Success(_))
    }
  }
}

object Validation extends Validation{
  type ValidatedEx[T] = ValidatedNel[Throwable, T]
}
