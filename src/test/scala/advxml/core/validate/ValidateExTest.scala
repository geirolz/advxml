package advxml.core.validate

import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class ValidateExTest extends AnyFunSuite with ValidatedExAsserts {

  //Transform
  test("Test ValidatedEx.transformE[Try] - Valid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Valid(ValidatedNelEx.transformE[Try, String](_))
  }

  test("Test ValidatedEx.transformE[Try] - Invalid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Invalid(ValidatedNelEx.transformE[Try, String](_))
  }

  test("Test ValidatedEx.transformE[EitherEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Valid(ValidatedNelEx.transformE[EitherEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Invalid(ValidatedNelEx.transformE[EitherEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Valid(ValidatedNelEx.transformNE[EitherNelEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Invalid(ValidatedNelEx.transformNE[EitherNelEx, String](_))
  }

  test("Test ValidatedEx.transformA[Option] - Valid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Valid(ValidatedNelEx.transformA[Option, String](_))
  }

  test("Test ValidatedEx.transformA[Option] - Invalid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Invalid(ValidatedNelEx.transformA[Option, String](_))
  }

  //Converters
  //Try
  test("Test fromTry - Success") {
    assert_Try_Success(ValidatedNelEx.fromTry)
  }

  test("Test fromTry - Failure") {
    assert_Try_Failure(ValidatedNelEx.fromTry)
  }

  //Either
  test("Test fromEither - Right") {
    assert_EitherEx_Right(ValidatedNelEx.fromEither)
  }

  test("Test fromEither - Left") {
    assert_EitherEx_Left(ValidatedNelEx.fromEither)
  }

  test("Test fromEitherNel - Right") {
    assert_EitherNelEx_Right(ValidatedNelEx.fromEitherNel)
  }

  test("Test fromEitherNel - Left") {
    assert_EitherNelEx_Left(ValidatedNelEx.fromEitherNel)
  }

  //Option
  test("Test fromOption - Some") {
    assert_Option_Some((optionValue, ex) => ValidatedNelEx.fromOption(optionValue, ex))
  }

  test("Test fromOption - None") {
    assert_Option_None((optionValue, ex) => ValidatedNelEx.fromOption(optionValue, ex))
  }
}

private[advxml] trait ValidatedExAsserts {

  //Utils
  private val TEST_EXCEPTION = new RuntimeException("TEXT_EX")
  private val TEST_EXCEPTION_NEL = NonEmptyList.of(
    new RuntimeException("TEXT_EX_1"),
    new RuntimeException("TEXT_EX_2")
  )

  def assert_ValidatedEx_to_Try_Valid(f: ValidatedNelEx[String] => Try[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
    val result: Try[String] = f(validatedExValue)

    assert(result == Success(value))
  }

  def assert_ValidatedEx_to_Try_Invalid(f: ValidatedNelEx[String] => Try[String]): Unit = {

    val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result = f(validatedExValue)

    assert(result.isFailure)
  }

  def assert_ValidatedEx_to_EitherEx_Valid(f: ValidatedNelEx[String] => EitherEx[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
    val result: EitherEx[String] = f(validatedExValue)

    assert(result == Right(value))
  }

  def assert_ValidatedEx_to_EitherEx_Invalid(f: ValidatedNelEx[String] => EitherEx[String]): Unit = {
    val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result: EitherEx[String] = f(validatedExValue)

    assert(result.isLeft)
  }

  def assert_ValidatedEx_to_EitherNelEx_Valid(f: ValidatedNelEx[String] => EitherNelEx[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
    val result: EitherNelEx[String] = f(validatedExValue)

    assert(result == Right(value))
  }

  def assert_ValidatedEx_to_EitherNelEx_Invalid(f: ValidatedNelEx[String] => EitherNelEx[String]): Unit = {
    val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)

    val result: EitherNelEx[String] = f(validatedExValue)

    assert(result.isLeft)
  }

  def assert_ValidatedEx_to_Option_Valid(f: ValidatedNelEx[String] => Option[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedNelEx[String] = Validated.Valid(value)
    val result: Option[String] = f(validatedExValue)

    assert(result.contains(value))
  }

  def assert_ValidatedEx_to_Option_Invalid(f: ValidatedNelEx[String] => Option[String]): Unit = {
    val validatedExValue: ValidatedNelEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result: Option[String] = f(validatedExValue)

    assert(result.isEmpty)
  }

  def assert_Try_Success(f: Try[String] => ValidatedNelEx[String]): Unit = {
    val value = "TEST"
    val tryValue: Try[String] = Success(value)
    val validatedExValue = f(tryValue)

    assertValid(validatedExValue, value)
  }

  def assert_Try_Failure(f: Try[String] => ValidatedNelEx[String]): Unit = {
    val tryValue: Try[String] = Failure(TEST_EXCEPTION)
    val validatedExValue = f(tryValue)

    assertInvalid(validatedExValue)
  }

  def assert_EitherEx_Right(f: EitherEx[String] => ValidatedNelEx[String]): Unit = {
    val value = "TEST"
    val eitherValue: EitherEx[String] = Right(value)
    val validatedExValue = f(eitherValue)

    assertValid(validatedExValue, value)
  }

  def assert_EitherEx_Left(f: EitherEx[String] => ValidatedNelEx[String]): Unit = {
    val eitherValue: EitherEx[String] = Left(TEST_EXCEPTION)
    val validatedExValue = f(eitherValue)

    assertInvalid(validatedExValue)
  }

  private def assertInvalid(v: ValidatedNelEx[_]): Unit = assert(v.isInvalid)

  def assert_EitherNelEx_Right(f: EitherNelEx[String] => ValidatedNelEx[String]): Unit = {
    val value = "TEST"
    val eitherValue: EitherNelEx[String] = Right(value)
    val validatedExValue = f(eitherValue)

    assertValid(validatedExValue, value)
  }

  def assert_EitherNelEx_Left(f: EitherNelEx[String] => ValidatedNelEx[String]): Unit = {

    val eitherValue: EitherNelEx[String] = Left(TEST_EXCEPTION_NEL)
    val validatedExValue = f(eitherValue)

    assertInvalid(validatedExValue)
  }

  def assert_Option_Some(f: (Option[String], Throwable) => ValidatedNelEx[String]): Unit = {
    val value = "TEST"
    val optionValue = Some(value)
    val validatedExValue = f(optionValue, TEST_EXCEPTION)

    assertValid(validatedExValue, value)
  }

  private def assertValid[T](v: ValidatedNelEx[T], expectedValue: => T): Unit = {
    assert(v == Valid(expectedValue))
  }

  def assert_Option_None(f: (Option[String], Throwable) => ValidatedNelEx[String]): Unit = {
    val optionValue: Option[String] = None
    val validatedExValue: ValidatedNelEx[String] = f(optionValue, TEST_EXCEPTION)

    assertInvalid(validatedExValue)
  }
}
