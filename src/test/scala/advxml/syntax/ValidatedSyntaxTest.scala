package advxml.syntax

import advxml.core.data.{EitherEx, EitherNelEx, ValidateExTest, ValidatedEx, ValidatedNelEx}
import advxml.core.data.ValidateExTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ValidatedSyntaxTest extends AnyFunSuite with FunSuiteContract {

  // format: off
  ValidateExTest.Contract(
    "Syntax",
    {
      import advxml.instances.data._
      import advxml.syntax.all._
      import cats.instances.either._
      import cats.instances.option._
      import cats.instances.try_._
      
      ContractFuncs(
        toTry           = _.to[Try],
        fromTry         = _.to[ValidatedNelEx],
        //=======
        toEitherEx      = _.to[EitherEx],
        fromEitherEx    = _.to[ValidatedNelEx],
        //=======
        toEitherNelEx   = _.to[EitherNelEx],
        fromEitherNelEx = _.to[ValidatedNelEx],
        //=======
        toValidatedEx   = _.to[ValidatedEx],
        fromValidatedEx = _.to[ValidatedNelEx],
        //=======
        toOption        = _.to[Option],
        fromOption      = (optionValue, ex) => optionValue.to[ValidatedNelEx](ex)
      )
    }
  ).runAll()
  // format: on
}
