package advxml.data

import advxml.data.error.AggregatedException
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

/** Advxml Created by geirolad on 29/07/2019.
  *
  * @author
  *   geirolad
  */
class AggregatedExceptionTest extends AnyFunSuite {

  test("Test printStackTrace") {
    val ex = AggregatedException(
      NonEmptyList.of(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    ex.printStackTrace()
  }

  test("Test getStackTraces - Has size == 3") {
    val ex = AggregatedException(
      NonEmptyList.of(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    val result = ex.getStackTraces
    result.size shouldBe 3
  }

  test("Test getStackTrace - non Empty") {
    val ex = AggregatedException(
      NonEmptyList.of(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    assert(ex.getStackTrace.nonEmpty)
  }

  test("Test setStackTrace - should throw UnsupportedOperationException") {
    val ex = AggregatedException(
      NonEmptyList.of(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    assertThrows[UnsupportedOperationException](ex.setStackTrace(Array.empty))
  }
}
