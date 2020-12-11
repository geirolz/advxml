package advxml.core

import cats.Eq
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

import scala.util.{Failure, Success, Try}

class MonadExOrEuTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import cats.instances.option._
  import cats.instances.try_._
  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual
  private val exception = new RuntimeException("ERROR")

  checkAll(
    "MonadExOrEu[Try]",
    cats.laws.discipline
      .MonadTests[Try](MonadExOrEu[Try])
      .monad[Int, Int, Int]
  )

  checkAll(
    "MonadExOrEu[Option]",
    cats.laws.discipline
      .MonadTests[Option](MonadExOrEu[Option])
      .monad[Int, Int, Int]
  )

  test("MonadExOrEu[Try].raiseErrorOrEmpty") {
    val value: Try[Int] = MonadExOrEu[Try].raiseErrorOrEmpty(exception)
    assert(value.isFailure)
  }

  test("MonadExOrEu[Option].raiseErrorOrEmpty") {
    val value: Option[Int] = MonadExOrEu[Option].raiseErrorOrEmpty(exception)
    assert(value.isEmpty)
  }

  test("MonadExOrEu[Try].raiseError") {
    val value: Try[Int] = MonadExOrEu[Try].asInstanceOf[MonadExCase[Try]].raiseError(exception)
    assert(value.isFailure)
  }

  test("MonadExOrEu[Option].empty") {
    val value: Option[Int] = MonadExOrEu[Option].asInstanceOf[MonadEuCase[Option]].empty
    assert(value.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("MonadExOrEu.fromOption") {
    val valueTrySuccess: Try[Int] = MonadExOrEu.fromOption[Try, Int](exception)(Some(1))
    val valueTryFailure: Try[Int] = MonadExOrEu.fromOption[Try, Int](exception)(None)
    val valueOptSome: Option[Int] = MonadExOrEu.fromOption[Option, Int](exception)(Some(1))
    val valueOptNone: Option[Int] = MonadExOrEu.fromOption[Option, Int](exception)(None)

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("MonadExOrEu.fromTry") {
    val valueTrySuccess: Try[Int] = MonadExOrEu.fromTry[Try, Int](Success(1))
    val valueTryFailure: Try[Int] = MonadExOrEu.fromTry[Try, Int](Failure(exception))
    val valueOptSome: Option[Int] = MonadExOrEu.fromTry[Option, Int](Success(1))
    val valueOptNone: Option[Int] = MonadExOrEu.fromTry[Option, Int](Failure(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("MonadExOrEu.fromEither") {
    val valueTrySuccess: Try[Int] = MonadExOrEu.fromEither[Try, Int](Right(1))
    val valueTryFailure: Try[Int] = MonadExOrEu.fromEither[Try, Int](Left(exception))
    val valueOptSome: Option[Int] = MonadExOrEu.fromEither[Option, Int](Right(1))
    val valueOptNone: Option[Int] = MonadExOrEu.fromEither[Option, Int](Left(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }

  //noinspection OptionEqualsSome
  test("MonadExOrEu.fromValidated") {
    val valueTrySuccess: Try[Int] = MonadExOrEu.fromValidated[Try, Int](Valid(1))
    val valueTryFailure: Try[Int] = MonadExOrEu.fromValidated[Try, Int](Invalid(exception))
    val valueOptSome: Option[Int] = MonadExOrEu.fromValidated[Option, Int](Valid(1))
    val valueOptNone: Option[Int] = MonadExOrEu.fromValidated[Option, Int](Invalid(exception))

    assert(valueTrySuccess == Success(1))
    assert(valueTryFailure.isFailure)
    assert(valueOptSome == Some(1))
    assert(valueOptNone.isEmpty)
  }
}
