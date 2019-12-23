package advxml.core.validate

import cats.data.{NonEmptyList, Validated}
import org.scalatest.FunSuite

import scala.util.{Failure, Success, Try}

class ValidateExTest extends FunSuite with ValidatedExAsserts {

  //Transform
  test("Test ValidatedEx.transformE[Try] - Valid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Valid(ValidatedEx.transformE[Try, String](_))
  }

  test("Test ValidatedEx.transformE[Try] - Invalid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Invalid(ValidatedEx.transformE[Try, String](_))
  }

  test("Test ValidatedEx.transformE[EitherEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Valid(ValidatedEx.transformE[EitherEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Invalid(ValidatedEx.transformE[EitherEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Valid(ValidatedEx.transformNE[EitherNelEx, String](_))
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Invalid(ValidatedEx.transformNE[EitherNelEx, String](_))
  }

  test("Test ValidatedEx.transformA[Option] - Valid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Valid(ValidatedEx.transformA[Option, String](_))
  }

  test("Test ValidatedEx.transformA[Option] - Invalid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Invalid(ValidatedEx.transformA[Option, String](_))
  }

  //Converters
  //Try
  test("Test fromTry - Success") {
    assert_Try_Success(ValidatedEx.fromTry)
  }

  test("Test fromTry - Failure") {
    assert_Try_Failure(ValidatedEx.fromTry)
  }

  //Either
  test("Test fromEither - Right") {
    assert_EitherEx_Right(ValidatedEx.fromEither)
  }

  test("Test fromEither - Left") {
    assert_EitherEx_Left(ValidatedEx.fromEither)
  }

  test("Test fromEitherNel - Right") {
    assert_EitherNelEx_Right(ValidatedEx.fromEitherNel)
  }

  test("Test fromEitherNel - Left") {
    assert_EitherNelEx_Left(ValidatedEx.fromEitherNel)
  }

  //Option
  test("Test fromOption - Some") {
    assert_Option_Some((optionValue, ex) => ValidatedEx.fromOption(optionValue, ex))
  }

  test("Test fromOption - None") {
    assert_Option_None((optionValue, ex) => ValidatedEx.fromOption(optionValue, ex))
  }
}

private[advxml] trait ValidatedExAsserts {

  //Utils
  private val TEST_EXCEPTION = new RuntimeException("TEXT_EX")
  private val TEST_EXCEPTION_NEL = NonEmptyList.of(
    new RuntimeException("TEXT_EX_1"),
    new RuntimeException("TEXT_EX_2")
  )
  private def assertValid[T](v: ValidatedEx[T], expectedValue: => T): Unit = {
    assert(v.isValid)
    assert(v.toOption.get == expectedValue)
  }
  private def assertInvalid(v: ValidatedEx[_]): Unit = assert(v.isInvalid)

  def assert_ValidatedEx_to_Try_Valid(f: ValidatedEx[String] => Try[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedEx[String] = Validated.Valid(value)
    val result = f(validatedExValue)

    assert(result.isSuccess)
    assert(result.get == value)
  }

  def assert_ValidatedEx_to_Try_Invalid(f: ValidatedEx[String] => Try[String]): Unit = {

    val validatedExValue: ValidatedEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result = f(validatedExValue)

    assert(result.isFailure)
  }

  def assert_ValidatedEx_to_EitherEx_Valid(f: ValidatedEx[String] => EitherEx[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedEx[String] = Validated.Valid(value)
    val result: EitherEx[String] = f(validatedExValue)

    assert(result.isRight)
    assert(result.toOption.get == value)
  }

  def assert_ValidatedEx_to_EitherEx_Invalid(f: ValidatedEx[String] => EitherEx[String]): Unit = {
    val validatedExValue: ValidatedEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result: EitherEx[String] = f(validatedExValue)

    assert(result.isLeft)
  }

  def assert_ValidatedEx_to_EitherNelEx_Valid(f: ValidatedEx[String] => EitherNelEx[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedEx[String] = Validated.Valid(value)
    val result: EitherNelEx[String] = f(validatedExValue)

    assert(result.isRight)
    assert(result.toOption.get == value)
  }

  def assert_ValidatedEx_to_EitherNelEx_Invalid(f: ValidatedEx[String] => EitherNelEx[String]): Unit = {
    val validatedExValue: ValidatedEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)

    val result: EitherNelEx[String] = f(validatedExValue)

    assert(result.isLeft)
  }

  def assert_ValidatedEx_to_Option_Valid(f: ValidatedEx[String] => Option[String]): Unit = {
    val value = "TEST"
    val validatedExValue: ValidatedEx[String] = Validated.Valid(value)
    val result: Option[String] = f(validatedExValue)

    assert(result.isDefined)
    assert(result.get == value)
  }

  def assert_ValidatedEx_to_Option_Invalid(f: ValidatedEx[String] => Option[String]): Unit = {
    val validatedExValue: ValidatedEx[String] = Validated.Invalid(TEST_EXCEPTION_NEL)
    val result: Option[String] = f(validatedExValue)

    assert(result.isEmpty)
  }

  def assert_Try_Success(f: Try[String] => ValidatedEx[String]): Unit = {
    val value = "TEST"
    val tryValue: Try[String] = Success(value)
    val validatedExValue = f(tryValue)

    assertValid(validatedExValue, value)
  }

  def assert_Try_Failure(f: Try[String] => ValidatedEx[String]): Unit = {
    val tryValue: Try[String] = Failure(TEST_EXCEPTION)
    val validatedExValue = f(tryValue)

    assertInvalid(validatedExValue)
  }

  def assert_EitherEx_Right(f: EitherEx[String] => ValidatedEx[String]): Unit = {
    val value = "TEST"
    val eitherValue: EitherEx[String] = Right(value)
    val validatedExValue = f(eitherValue)

    assertValid(validatedExValue, value)
  }

  def assert_EitherEx_Left(f: EitherEx[String] => ValidatedEx[String]): Unit = {
    val eitherValue: EitherEx[String] = Left(TEST_EXCEPTION)
    val validatedExValue = f(eitherValue)

    assertInvalid(validatedExValue)
  }

  def assert_EitherNelEx_Right(f: EitherNelEx[String] => ValidatedEx[String]): Unit = {
    val value = "TEST"
    val eitherValue: EitherNelEx[String] = Right(value)
    val validatedExValue = f(eitherValue)

    assertValid(validatedExValue, value)
  }

  def assert_EitherNelEx_Left(f: EitherNelEx[String] => ValidatedEx[String]): Unit = {

    val eitherValue: EitherNelEx[String] = Left(TEST_EXCEPTION_NEL)
    val validatedExValue = f(eitherValue)

    assertInvalid(validatedExValue)
  }

  def assert_Option_Some(f: (Option[String], Throwable) => ValidatedEx[String]): Unit = {
    val value = "TEST"
    val optionValue = Some(value)
    val validatedExValue = f(optionValue, TEST_EXCEPTION)

    assertValid(validatedExValue, value)
  }

  def assert_Option_None(f: (Option[String], Throwable) => ValidatedEx[String]): Unit = {
    val optionValue: Option[String] = None
    val validatedExValue: ValidatedEx[String] = f(optionValue, TEST_EXCEPTION)

    assertInvalid(validatedExValue)
  }
}
