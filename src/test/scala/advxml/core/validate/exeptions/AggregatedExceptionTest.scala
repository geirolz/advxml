package advxml.core.validate.exeptions

import advxml.core.validate.exceptions.AggregatedException
import org.scalatest.FunSuite

/**
  * Advxml
  * Created by geirolad on 29/07/2019.
  *
  * @author geirolad
  */
class AggregatedExceptionTest extends FunSuite {

  test("Test printStackTrace") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    ex.printStackTrace()
  }

  test("Test getStackTraces - Has size == 3") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    val result = ex.getStackTraces
    assert(result.size == 3)
  }

  test("Test getStackTrace - non Empty") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    assert(ex.getStackTrace.nonEmpty)
  }

  test("Test setStackTrace - should throw UnsupportedOperationException") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    assertThrows[UnsupportedOperationException](ex.setStackTrace(Array.empty))
  }
}
