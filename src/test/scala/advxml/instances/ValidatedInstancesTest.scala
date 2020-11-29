package advxml.instances

import advxml.core.data.ValidatedNelEx
import advxml.core.data.error.AggregatedException
import cats.Eq
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

class ValidatedInstancesTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import advxml.instances.validated._
  import cats.implicits._
  import cats.laws.discipline.arbitrary._

  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual

  test("Test throwable_to_ThrowableNel") {
    val ex = new RuntimeException("TEST")
    val result = throwableToThrowableNel(ex)
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

    val result = throwableToThrowableNel(ex)
    assert(result.size == 2)
    assert(result.get(0).get.getMessage == "TEST1")
    assert(result.get(1).get.getMessage == "TEST2")
  }

  test("Test throwableNel_to_Throwable") {
    val exs = NonEmptyList.of(
      new RuntimeException("TEST1"),
      new RuntimeException("TEST2")
    )
    val result: AggregatedException = throwableNelToThrowable(exs).asInstanceOf[AggregatedException]
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
}
