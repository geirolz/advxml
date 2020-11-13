package advxml.syntax

import advxml.core.ValidateExTest.ContractFuncs
import advxml.core.{EitherEx, EitherNelEx, ValidateExTest}
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ValidationSyntaxTest extends AnyFunSuite with FunSuiteContract {

  // format: off
  ValidateExTest.Contract(
    "Syntax",
    {
      import cats.instances.either._
      import cats.instances.option._
      import cats.instances.try_._
      
      ContractFuncs(
        toTry           = _.transformE[Try],
        fromTry         = _.toValidatedEx,
        toEitherEx      = _.transformE[EitherEx],
        fromEitherEx    = _.toValidatedEx,
        toEitherNelEx   = _.transformNE[EitherNelEx],
        fromEitherNelEx = _.toValidatedEx,
        toOption        = _.transformA[Option],
        fromOption      = (optionValue, ex) => optionValue.toValidatedEx(ex)
      )
    }
  ).runAll()
  // format: on
}
