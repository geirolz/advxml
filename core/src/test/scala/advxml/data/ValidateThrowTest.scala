package advxml.data

import advxml.data.ValidateThrowTest.ContractFuncs
import advxml.testing.{ContractTests, FunSuiteContract}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class ValidateThrowTest extends AnyFunSuite with FunSuiteContract {

  import advxml.implicits.*
  import cats.instances.either.*
  import cats.instances.option.*
  import cats.instances.try_.*

  // format: off
  ValidateThrowTest.Contract(
    f = ContractFuncs(
      toTry           = _.to[Try],
      fromTry         = ValidatedNelThrow.fromTry,
      //===========
      toEitherThrow      = _.to[EitherThrow],
      fromEitherThrow    = ValidatedNelThrow.fromEither,
      //===========
      toEitherNelThrow   = _.to[EitherNelThrow],
      fromEitherNelThrow = ValidatedNelThrow.fromEitherNel,
      //===========
      toValidatedThrow   = _.to[ValidatedThrow],
      fromValidatedThrow = _.to[ValidatedNelThrow],
      //===========
      toOption        = _.to[Option],
      fromOption      = (optionValue, ex) => ValidatedNelThrow.fromOption(optionValue, ex)
    )
  ).runAll()
  // format: on
}

object ValidateThrowTest {

  case class ContractFuncs(
    toTry: ValidatedNelThrow[String] => Try[String],
    fromTry: Try[String] => ValidatedNelThrow[String],
    // ===========
    toEitherThrow: ValidatedNelThrow[String] => EitherThrow[String],
    fromEitherThrow: EitherThrow[String] => ValidatedNelThrow[String],
    // ===========
    toEitherNelThrow: ValidatedNelThrow[String] => EitherNelThrow[String],
    fromEitherNelThrow: EitherNelThrow[String] => ValidatedNelThrow[String],
    // ===========
    toValidatedThrow: ValidatedNelThrow[String] => ValidatedThrow[String],
    fromValidatedThrow: ValidatedThrow[String] => ValidatedNelThrow[String],
    // ===========
    toOption: ValidatedNelThrow[String] => Option[String],
    fromOption: (Option[String], Throwable) => ValidatedNelThrow[String]
  )

  case class Contract(subDesc: String = "", f: ContractFuncs)
      extends ContractTests("ValidateEx", subDesc) {

    // Utils
    private val TEST_EXCEPTION = new RuntimeException("TEXT_EX")
    private val TEST_EXCEPTION_NEL = NonEmptyList.of(
      new RuntimeException("TEXT_EX_1"),
      new RuntimeException("TEXT_EX_2")
    )

    private def assertInvalid(v: ValidatedNelThrow[?]): Unit =
      assert(v.isInvalid)

    private def assertValid[T](v: ValidatedNelThrow[T], expectedValue: => T): Unit =
      assert(v == Valid(expectedValue))

    // ============================== TO ==============================
    test("Valid.toTry") {
      val value                                          = "TEST"
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Valid(value)
      val result: Try[String]                            = f.toTry(validatedThrowValue)

      assert(result == Success(value))
    }

    test("Invalid.toTry") {
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: Try[String]                            = f.toTry(validatedThrowValue)

      assert(result.isFailure)
    }

    test("Valid.toEitherThrow") {
      val value                                          = "TEST"
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Valid(value)
      val result: EitherThrow[String]                    = f.toEitherThrow(validatedThrowValue)

      assert(result == Right(value))
    }

    test("Invalid.toEitherThrow") {
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: EitherThrow[String]                    = f.toEitherThrow(validatedThrowValue)

      assert(result.isLeft)
    }

    test("Valid.toEitherNelThrow") {
      val value                                          = "TEST"
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Valid(value)
      val result: EitherNelThrow[String]                 = f.toEitherNelThrow(validatedThrowValue)

      assert(result == Right(value))
    }

    test("Invalid.toEitherNelThrow") {
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: EitherNelThrow[String]                 = f.toEitherNelThrow(validatedThrowValue)

      assert(result.isLeft)
    }

    test("Valid.toValidatedThrow") {
      val value                                          = "TEST"
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Valid(value)
      val result: ValidatedThrow[String]                 = f.toValidatedThrow(validatedThrowValue)

      assert(result == Valid(value))
    }

    test("Invalid.toValidatedThrow") {
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: ValidatedThrow[String]                 = f.toValidatedThrow(validatedThrowValue)

      assert(result.isInvalid)
    }

    test("Valid.toOption") {
      val value                                          = "TEST"
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Valid(value)
      val result: Option[String]                         = f.toOption(validatedThrowValue)

      assert(result.contains(value))
    }

    test("Invalid.toOption") {
      val validatedThrowValue: ValidatedNelThrow[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: Option[String]                         = f.toOption(validatedThrowValue)

      assert(result.isEmpty)
    }

    // ============================== FROM ==============================
    test("Try.Success.toValidatedThrow") {
      val value                                          = "TEST"
      val tryValue: Try[String]                          = Success(value)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromTry(tryValue)

      assertValid(validatedThrowValue, value)
    }

    test("Try.Failure.toValidatedThrow") {
      val tryValue: Try[String]                          = Failure(TEST_EXCEPTION)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromTry(tryValue)

      assertInvalid(validatedThrowValue)
    }

    test("EitherThrow.Right.toValidatedThrow") {
      val value                                          = "TEST"
      val eitherValue: EitherThrow[String]               = Right(value)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromEitherThrow(eitherValue)

      assertValid(validatedThrowValue, value)
    }

    test("EitherThrow.Left.toValidatedThrow") {
      val eitherValue: EitherThrow[String]               = Left(TEST_EXCEPTION)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromEitherThrow(eitherValue)

      assertInvalid(validatedThrowValue)
    }

    test("EitherNelThrow.Right.toValidatedThrow") {
      val value                                          = "TEST"
      val eitherValue: EitherNelThrow[String]            = Right(value)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromEitherNelThrow(eitherValue)

      assertValid(validatedThrowValue, value)
    }

    test("EitherNelThrow.Left.toValidatedThrow") {
      val eitherValue: EitherNelThrow[String]            = Left(TEST_EXCEPTION_NEL)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromEitherNelThrow(eitherValue)

      assertInvalid(validatedThrowValue)
    }

    test("ValidatedThrow.Valid.toValidatedThrow") {
      val value                                          = "TEST"
      val eitherValue: ValidatedThrow[String]            = Valid(value)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromValidatedThrow(eitherValue)

      assertValid(validatedThrowValue, value)
    }

    test("ValidatedThrow.Invalid.toValidatedThrow") {
      val eitherValue: ValidatedThrow[String]            = Invalid(TEST_EXCEPTION)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromValidatedThrow(eitherValue)

      assertInvalid(validatedThrowValue)
    }

    test("Option.Some.toValidatedThrow") {
      val value                                          = "TEST"
      val optionValue                                    = Some(value)
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromOption(optionValue, TEST_EXCEPTION)

      assertValid(validatedThrowValue, value)
    }

    test("Option.None.toValidatedThrow") {
      val optionValue: Option[String]                    = None
      val validatedThrowValue: ValidatedNelThrow[String] = f.fromOption(optionValue, TEST_EXCEPTION)

      assertInvalid(validatedThrowValue)
    }
  }
}
