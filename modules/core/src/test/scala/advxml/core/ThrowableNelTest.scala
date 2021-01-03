package advxml.core

import advxml.core.data.error.AggregatedException
import advxml.core.data.ThrowableNel
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite

class ThrowableNelTest extends AnyFunSuite {

  import cats.implicits._

  test("ThrowableNel.fromThrowable") {
    val ex: RuntimeException = new RuntimeException("TEST")
    val result: ThrowableNel = ThrowableNel.fromThrowable(ex)
    assert(result.head == ex)
    assert(result.size == 1)
  }

  test("ThrowableNel.fromThrowable with AggregatedException") {
    val ex: AggregatedException = AggregatedException(
      NonEmptyList.of(
        new RuntimeException("TEST1"),
        new RuntimeException("TEST2")
      )
    )

    val result: ThrowableNel = ThrowableNel.fromThrowable(ex)
    assert(result.size == 2)
    assert(result.get(0).get.getMessage == "TEST1")
    assert(result.get(1).get.getMessage == "TEST2")
  }

  test("ThrowableNel.toThrowable") {
    val exs: NonEmptyList[RuntimeException] = NonEmptyList.of(
      new RuntimeException("TEST1"),
      new RuntimeException("TEST2")
    )
    val result: AggregatedException = ThrowableNel
      .toThrowable(exs)
      .asInstanceOf[AggregatedException]

    assert(result.exceptions.size == 2)
    assert(result.exceptions.get(0).get.getMessage == "TEST1")
    assert(result.exceptions.get(1).get.getMessage == "TEST2")
  }
}
