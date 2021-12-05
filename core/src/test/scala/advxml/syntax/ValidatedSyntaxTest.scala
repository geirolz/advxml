package advxml.syntax

import advxml.data.{
  EitherNelThrow,
  EitherThrow,
  ValidateThrowTest,
  ValidatedNelThrow,
  ValidatedThrow
}
import advxml.data.ValidateThrowTest.ContractFuncs
import advxml.testing.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ValidatedSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.implicits.*

  // format: off
  ValidateThrowTest.Contract(
    "Syntax",
    {
      import advxml.data.Converter.*
      import advxml.data.error.AggregatedException.*
      import cats.instances.either.*
      import cats.instances.option.*
      import cats.instances.try_.*
      
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
