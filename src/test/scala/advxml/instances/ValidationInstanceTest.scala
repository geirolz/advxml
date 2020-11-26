package advxml.instances

import advxml.core.validate.{ThrowableNel, ValidatedNelEx}
import advxml.core.validate.exceptions.AggregatedException
import cats.Eq
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

class ValidationInstanceTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import advxml.instances.validate._
  import cats.laws.discipline.arbitrary._

  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual

  test("Test throwable_to_ThrowableNel") {
    val ex = new RuntimeException("TEST")
    val result = throwable_to_ThrowableNel(ex)
    assert(result.head == ex)
    assert(result.size == 1)
  }

  test("Test throwable_to_ThrowableNel with AggregatedException") {
    val ex = new AggregatedException(
      NonEmptyList.of(
        new RuntimeException("TEST1"),
        new RuntimeException("TEST2")
      )
    )

    val result = throwable_to_ThrowableNel(ex)
    assert(result.size == 2)
    assert(result.get(0).get.getMessage == "TEST1")
    assert(result.get(1).get.getMessage == "TEST2")
  }

  test("Test throwableNel_to_Throwable") {
    val exs = NonEmptyList.of(
      new RuntimeException("TEST1"),
      new RuntimeException("TEST2")
    )
    val result: AggregatedException = throwableNel_to_Throwable(exs).asInstanceOf[AggregatedException]
    assert(result.exceptions.size == 2)
    assert(result.exceptions.get(0).get.getMessage == "TEST1")
    assert(result.exceptions.get(1).get.getMessage == "TEST2")
  }

  checkAll(
    "MonadTests[ValidatedEx, Throwable]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedNelEx, Throwable]
      .monad[Int, Int, Int] //TODO: monadError
  )

  checkAll(
    "MonadTests[ValidatedEx, ThrowableNel]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedNelEx, ThrowableNel]
      .monadError[Int, Int, Int]
  )
}
