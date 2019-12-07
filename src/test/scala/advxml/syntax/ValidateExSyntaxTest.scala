package advxml.syntax

import advxml.core.validate.{EitherEx, EitherNelEx, ValidatedExAsserts}
import org.scalatest.FunSuite

import scala.util.Try

class ValidateExSyntaxTest extends FunSuite with ValidatedExAsserts {

  import advxml.syntax.validate._

  //Transform
  test("Test ValidatedEx.transformE[Try] - Valid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Valid(_.transformE[Try])
  }

  test("Test ValidatedEx.transformE[Try] - Invalid") {
    import cats.instances.try_._
    assert_ValidatedEx_to_Try_Invalid(_.transformE[Try])
  }

  test("Test ValidatedEx.transformE[EitherEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Valid(_.transformE[EitherEx])
  }

  test("Test ValidatedEx.transformE[EitherEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherEx_Invalid(_.transformE[EitherEx])
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Valid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Valid(_.transformNE[EitherNelEx])
  }

  test("Test ValidatedEx.transformE[EitherNelEx] - Invalid") {
    import cats.instances.either._
    assert_ValidatedEx_to_EitherNelEx_Invalid(_.transformNE[EitherNelEx])
  }

  test("Test ValidatedEx.transformA[Option] - Valid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Valid(_.transformA[Option])
  }

  test("Test ValidatedEx.transformA[Option] - Invalid") {
    import cats.instances.option._
    assert_ValidatedEx_to_Option_Valid(_.transformA[Option])
  }

  //Try
  test("Test Try.toValidatedEx - Success") {
    assert_Try_Success(_.toValidatedEx)
  }

  test("Test Try.toValidatedEx - Failure") {
    assert_Try_Failure(_.toValidatedEx)
  }

  //Either
  test("Test EitherEx.toValidatedEx - Right") {
    assert_EitherEx_Right(_.toValidatedEx)
  }

  test("Test EitherEx.toValidatedEx - Left") {
    assert_EitherEx_Left(_.toValidatedEx)
  }

  test("Test EitherNelEx.toValidatedEx - Right") {
    assert_EitherNelEx_Right(_.toValidatedEx)
  }

  test("Test EitherNelEx.toValidatedEx - Left") {
    assert_EitherNelEx_Left(_.toValidatedEx)
  }

  //Option
  test("Test Option.toValidatedEx - Some") {
    assert_Option_Some((optionValue, ex) => optionValue.toValidatedEx(ex))
  }

  test("Test Option.toValidatedEx - None") {
    assert_Option_None((optionValue, ex) => optionValue.toValidatedEx(ex))
  }
}
