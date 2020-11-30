package advxml.syntax

import advxml.core.data.{EitherEx, ValidateExTest}
import advxml.core.data.ValidateExTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ValidatedSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.validated._

  // format: off
  ValidateExTest.Contract(
    "Syntax",
    {
      import cats.instances.either._
      import cats.instances.option._
      import cats.instances.try_._
      
      ContractFuncs(
        toTry           = _.transform[Try],
        fromTry         = _.toValidatedEx,
        toEitherEx      = _.transform[EitherEx],
        fromEitherEx    = _.toValidatedEx,
        fromEitherNelEx = _.toValidatedEx,
        toOption        = _.transform[Option],
        fromOption      = (optionValue, ex) => optionValue.toValidatedEx(ex)
      )
    }
  ).runAll()
  // format: on
}
