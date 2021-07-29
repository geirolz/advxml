package advxml.core.data

import advxml.core.data.ValidateExTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class ValidateExTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.all._
  import advxml.instances.data._
  import cats.instances.either._
  import cats.instances.option._
  import cats.instances.try_._

  // format: off
  ValidateExTest.Contract(
    f = ContractFuncs(
      toTry           = _.to[Try],
      fromTry         = ValidatedNelEx.fromTry,
      //===========
      toEitherThrow      = _.to[EitherThrow],
      fromEitherThrow    = ValidatedNelEx.fromEither,
      //===========
      toEitherNelThrow   = _.to[EitherNelThrow],
      fromEitherNelThrow = ValidatedNelEx.fromEitherNel,
      //===========
      toValidatedEx   = _.to[ValidatedEx],
      fromValidatedEx = _.to[ValidatedNelEx],
      //===========
      toOption        = _.to[Option],
      fromOption      = (optionValue, ex) => ValidatedNelEx.fromOption(optionValue, ex)
    )
  ).runAll()
  // format: on
}

object ValidateExTest {

  case class ContractFuncs(
    toTry: ValidatedNelEx[String] => Try[String],
    fromTry: Try[String] => ValidatedNelEx[String],
    //===========
    toEitherThrow: ValidatedNelEx[String] => EitherThrow[String],
    fromEitherThrow: EitherThrow[String] => ValidatedNelEx[String],
    //===========
    toEitherNelThrow: ValidatedNelEx[String] => EitherNelThrow[String],
    fromEitherNelThrow: EitherNelThrow[String] => ValidatedNelEx[String],
    //===========
    toValidatedEx: ValidatedNelEx[String] => ValidatedEx[String],
    fromValidatedEx: ValidatedEx[String] => ValidatedNelEx[String],
    //===========
    toOption: ValidatedNelEx[String] => Option[String],
    fromOption: (Option[String], Throwable) => ValidatedNelEx[String]
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("ValidateEx", subDesc) {

    //Utils
    private val TEST_EXCEPTION = new RuntimeException("TEXT_EX")
    private val TEST_EXCEPTION_NEL = NonEmptyList.of(
      new RuntimeException("TEXT_EX_1"),
      new RuntimeException("TEXT_EX_2")
    )

    private def assertInvalid(v: ValidatedNelEx[_]): Unit =
      assert(v.isInvalid)

    private def assertValid[T](v: ValidatedNelEx[T], expectedValue: => T): Unit =
      assert(v == Valid(expectedValue))

    //============================== TO ==============================
    test("Valid.toTry") {
      val value = "TEST"
      val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
      val result: Try[String] = f.toTry(validatedExValue)

      assert(result == Success(value))
    }

    test("Invalid.toTry") {
      val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: Try[String] = f.toTry(validatedExValue)

      assert(result.isFailure)
    }

    test("Valid.toEitherThrow") {
      val value = "TEST"
      val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
      val result: EitherThrow[String] = f.toEitherThrow(validatedExValue)

      assert(result == Right(value))
    }

    test("Invalid.toEitherThrow") {
      val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: EitherThrow[String] = f.toEitherThrow(validatedExValue)

      assert(result.isLeft)
    }

    test("Valid.toEitherNelThrow") {
      val value = "TEST"
      val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
      val result: EitherNelThrow[String] = f.toEitherNelThrow(validatedExValue)

      assert(result == Right(value))
    }

    test("Invalid.toEitherNelThrow") {
      val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: EitherNelThrow[String] = f.toEitherNelThrow(validatedExValue)

      assert(result.isLeft)
    }

    test("Valid.toValidatedEx") {
      val value = "TEST"
      val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
      val result: ValidatedEx[String] = f.toValidatedEx(validatedExValue)

      assert(result == Valid(value))
    }

    test("Invalid.toValidatedEx") {
      val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: ValidatedEx[String] = f.toValidatedEx(validatedExValue)

      assert(result.isInvalid)
    }

    test("Valid.toOption") {
      val value = "TEST"
      val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
      val result: Option[String] = f.toOption(validatedExValue)

      assert(result.contains(value))
    }

    test("Invalid.toOption") {
      val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
      val result: Option[String] = f.toOption(validatedExValue)

      assert(result.isEmpty)
    }

    //============================== FROM ==============================
    test("Try.Success.toValidatedEx") {
      val value = "TEST"
      val tryValue: Try[String] = Success(value)
      val validatedExValue: ValidatedNelEx[String] = f.fromTry(tryValue)

      assertValid(validatedExValue, value)
    }

    test("Try.Failure.toValidatedEx") {
      val tryValue: Try[String] = Failure(TEST_EXCEPTION)
      val validatedExValue: ValidatedNelEx[String] = f.fromTry(tryValue)

      assertInvalid(validatedExValue)
    }

    test("EitherThrow.Right.toValidatedEx") {
      val value = "TEST"
      val eitherValue: EitherThrow[String] = Right(value)
      val validatedExValue: ValidatedNelEx[String] = f.fromEitherThrow(eitherValue)

      assertValid(validatedExValue, value)
    }

    test("EitherThrow.Left.toValidatedEx") {
      val eitherValue: EitherThrow[String] = Left(TEST_EXCEPTION)
      val validatedExValue: ValidatedNelEx[String] = f.fromEitherThrow(eitherValue)

      assertInvalid(validatedExValue)
    }

    test("EitherNelThrow.Right.toValidatedEx") {
      val value = "TEST"
      val eitherValue: EitherNelThrow[String] = Right(value)
      val validatedExValue: ValidatedNelEx[String] = f.fromEitherNelThrow(eitherValue)

      assertValid(validatedExValue, value)
    }

    test("EitherNelThrow.Left.toValidatedEx") {
      val eitherValue: EitherNelThrow[String] = Left(TEST_EXCEPTION_NEL)
      val validatedExValue: ValidatedNelEx[String] = f.fromEitherNelThrow(eitherValue)

      assertInvalid(validatedExValue)
    }

    test("ValidatedEx.Valid.toValidatedEx") {
      val value = "TEST"
      val eitherValue: ValidatedEx[String] = Valid(value)
      val validatedExValue: ValidatedNelEx[String] = f.fromValidatedEx(eitherValue)

      assertValid(validatedExValue, value)
    }

    test("ValidatedEx.Invalid.toValidatedEx") {
      val eitherValue: ValidatedEx[String] = Invalid(TEST_EXCEPTION)
      val validatedExValue: ValidatedNelEx[String] = f.fromValidatedEx(eitherValue)

      assertInvalid(validatedExValue)
    }

    test("Option.Some.toValidatedEx") {
      val value = "TEST"
      val optionValue = Some(value)
      val validatedExValue: ValidatedNelEx[String] = f.fromOption(optionValue, TEST_EXCEPTION)

      assertValid(validatedExValue, value)
    }

    test("Option.None.toValidatedEx") {
      val optionValue: Option[String] = None
      val validatedExValue: ValidatedNelEx[String] = f.fromOption(optionValue, TEST_EXCEPTION)

      assertInvalid(validatedExValue)
    }
  }
}
