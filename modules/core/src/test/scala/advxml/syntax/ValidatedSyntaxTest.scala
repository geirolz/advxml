package advxml.syntax

import advxml.core.data.{
  EitherNelThrow,
  EitherThrow,
  ValidateThrowTest,
  ValidatedNelThrow,
  ValidatedThrow
}
import advxml.core.data.ValidateThrowTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ValidatedSyntaxTest extends AnyFunSuite with FunSuiteContract {

  // format: off
  ValidateThrowTest.Contract(
    "Syntax",
    {
      import advxml.instances.data._
      import advxml.syntax.all._
      import cats.instances.either._
      import cats.instances.option._
      import cats.instances.try_._
      
      ContractFuncs(
        toTry           = _.to[Try],
        fromTry         = _.to[ValidatedNelThrow],
        //=======
        toEitherThrow      = _.to[EitherThrow],
        fromEitherThrow    = _.to[ValidatedNelThrow],
        //=======
        toEitherNelThrow   = _.to[EitherNelThrow],
        fromEitherNelThrow = _.to[ValidatedNelThrow],
        //=======
        toValidatedThrow   = _.to[ValidatedThrow],
        fromValidatedThrow = _.to[ValidatedNelThrow],
        //=======
        toOption        = _.to[Option],
        fromOption      = (optionValue, ex) => optionValue.to[ValidatedNelThrow](ex)
      )
    }
  ).runAll()
  // format: on
}
