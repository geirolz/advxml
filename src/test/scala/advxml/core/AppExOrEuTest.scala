package advxml.core

import cats.Eq
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

import scala.util.{Failure, Success, Try}

class AppExOrEuTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import cats.instances.option._
  import cats.instances.try_._
  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual
  private val exception = new RuntimeException("ERROR")

  checkAll(
    "AppExOrEu[Try]",
    cats.laws.discipline
      .ApplicativeTests[Try](AppExOrEu[Try])
      .applicative[Int, Int, Int]
  )

  checkAll(
    "AppExOrEu[Option]",
    cats.laws.discipline
      .ApplicativeTests[Option](AppExOrEu[Option])
      .applicative[Int, Int, Int]
  )

  test("AppExOrEu[Try].raiseErrorOrEmpty") {
    val value: Try[Int] = AppExOrEu[Try].raiseErrorOrEmpty(exception)
    assert(value.isFailure)
  }

  test("AppExOrEu[Option].raiseErrorOrEmpty") {
    val value: Option[Int] = AppExOrEu[Option].raiseErrorOrEmpty(exception)
    assert(value.isEmpty)
  }

  test("AppExOrEu[Try].raiseError") {
    val value: Try[Int] = AppExOrEu[Try].raiseError(exception)
    assert(value.isFailure)
  }

  test("AppExOrEu[Option].empty") {
    val value: Option[Int] = AppExOrEu[Option].empty
    assert(value.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("AppExOrEu.fromOption") {
    val valueTrySuccess: Try[Int] = AppExOrEu.fromOption[Try, Int](exception)(Some(1))
    val valueTryFailure: Try[Int] = AppExOrEu.fromOption[Try, Int](exception)(None)
    val valueOptSome: Option[Int] = AppExOrEu.fromOption[Option, Int](exception)(Some(1))
    val valueOptNone: Option[Int] = AppExOrEu.fromOption[Option, Int](exception)(None)

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("AppExOrEu.fromTry") {
    val valueTrySuccess: Try[Int] = AppExOrEu.fromTry[Try, Int](Success(1))
    val valueTryFailure: Try[Int] = AppExOrEu.fromTry[Try, Int](Failure(exception))
    val valueOptSome: Option[Int] = AppExOrEu.fromTry[Option, Int](Success(1))
    val valueOptNone: Option[Int] = AppExOrEu.fromTry[Option, Int](Failure(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("AppExOrEu.fromEither") {
    val valueTrySuccess: Try[Int] = AppExOrEu.fromEitherEx[Try, Int](Right(1))
    val valueTryFailure: Try[Int] = AppExOrEu.fromEitherEx[Try, Int](Left(exception))
    val valueOptSome: Option[Int] = AppExOrEu.fromEitherEx[Option, Int](Right(1))
    val valueOptNone: Option[Int] = AppExOrEu.fromEitherEx[Option, Int](Left(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("AppExOrEu.fromValidated") {
    val valueTrySuccess: Try[Int] = AppExOrEu.fromValidatedEx[Try, Int](Valid(1))
    val valueTryFailure: Try[Int] = AppExOrEu.fromValidatedEx[Try, Int](Invalid(exception))
    val valueOptSome: Option[Int] = AppExOrEu.fromValidatedEx[Option, Int](Valid(1))
    val valueOptNone: Option[Int] = AppExOrEu.fromValidatedEx[Option, Int](Invalid(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }
}
