package advxml

import cats.Eq
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

import scala.util.{Failure, Success, Try}

class ApplicativeThrowOrEuTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import cats.instances.option.*
  import cats.instances.try_.*
  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual
  private val exception                   = new RuntimeException("ERROR")

  checkAll(
    "ApplicativeThrowOrEu[Try]",
    cats.laws.discipline
      .ApplicativeTests[Try](ApplicativeThrowOrEu[Try])
      .applicative[Int, Int, Int]
  )

  checkAll(
    "ApplicativeThrowOrEu[Option]",
    cats.laws.discipline
      .ApplicativeTests[Option](ApplicativeThrowOrEu[Option])
      .applicative[Int, Int, Int]
  )

  test("ApplicativeThrowOrEu[Try].raiseErrorOrEmpty") {
    val value: Try[Int] = ApplicativeThrowOrEu[Try].raiseErrorOrEmpty(exception)
    assert(value.isFailure)
  }

  test("ApplicativeThrowOrEu[Option].raiseErrorOrEmpty") {
    val value: Option[Int] = ApplicativeThrowOrEu[Option].raiseErrorOrEmpty(exception)
    assert(value.isEmpty)
  }

  test("ApplicativeThrowOrEu[Try].raiseError") {
    val value: Try[Int] = ApplicativeThrowOrEu[Try].raiseError(exception)
    assert(value.isFailure)
  }

  test("ApplicativeThrowOrEu[Option].empty") {
    val value: Option[Int] = ApplicativeThrowOrEu[Option].empty
    assert(value.isEmpty)
  }

  // noinspection OptionEqualsSome
  test("ApplicativeThrowOrEu.fromOption") {
    val valueTrySuccess: Try[Int] = ApplicativeThrowOrEu.fromOption[Try, Int](exception)(Some(1))
    val valueTryFailure: Try[Int] = ApplicativeThrowOrEu.fromOption[Try, Int](exception)(None)
    val valueOptSome: Option[Int] = ApplicativeThrowOrEu.fromOption[Option, Int](exception)(Some(1))
    val valueOptNone: Option[Int] = ApplicativeThrowOrEu.fromOption[Option, Int](exception)(None)

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  // noinspection OptionEqualsSome
  test("ApplicativeThrowOrEu.fromTry") {
    val valueTrySuccess: Try[Int] = ApplicativeThrowOrEu.fromTry[Try, Int](Success(1))
    val valueTryFailure: Try[Int] = ApplicativeThrowOrEu.fromTry[Try, Int](Failure(exception))
    val valueOptSome: Option[Int] = ApplicativeThrowOrEu.fromTry[Option, Int](Success(1))
    val valueOptNone: Option[Int] = ApplicativeThrowOrEu.fromTry[Option, Int](Failure(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  // noinspection OptionEqualsSome
  test("ApplicativeThrowOrEu.fromEither") {
    val valueTrySuccess: Try[Int] = ApplicativeThrowOrEu.fromEitherThrow[Try, Int](Right(1))
    val valueTryFailure: Try[Int] = ApplicativeThrowOrEu.fromEitherThrow[Try, Int](Left(exception))
    val valueOptSome: Option[Int] = ApplicativeThrowOrEu.fromEitherThrow[Option, Int](Right(1))
    val valueOptNone: Option[Int] =
      ApplicativeThrowOrEu.fromEitherThrow[Option, Int](Left(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  // noinspection OptionEqualsSome
  test("ApplicativeThrowOrEu.fromValidated") {
    val valueTrySuccess: Try[Int] = ApplicativeThrowOrEu.fromValidatedThrow[Try, Int](Valid(1))
    val valueTryFailure: Try[Int] =
      ApplicativeThrowOrEu.fromValidatedThrow[Try, Int](Invalid(exception))
    val valueOptSome: Option[Int] = ApplicativeThrowOrEu.fromValidatedThrow[Option, Int](Valid(1))
    val valueOptNone: Option[Int] =
      ApplicativeThrowOrEu.fromValidatedThrow[Option, Int](Invalid(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }
}
