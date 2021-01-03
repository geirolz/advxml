package advxml.instances

import advxml.core.data.error.AggregatedException
import cats.kernel.Semigroup
import org.scalatest.funsuite.AnyFunSuite

class DataAggregatedExceptionInstancesTest extends AnyFunSuite {

  test("Semigroup AggregatedException") {
    import advxml.instances.data._
    val ex1 = new RuntimeException("E1")
    val ex2 = new RuntimeException("E2")

    val result: List[Throwable] = Semigroup[Throwable]
      .combine(ex1, ex2)
      .asInstanceOf[AggregatedException]
      .exceptions
      .toList

    assert(result.contains(ex1))
    assert(result.contains(ex2))
  }
}
