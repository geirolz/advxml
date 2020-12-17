package advxml.core

import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class MonadExUtilsTest extends AnyFunSuite {
  test("implicit MonadEx.apply[Try]") {
    import cats.instances.try_._
    val monadExInstance = MonadEx[Try]
    assert(monadExInstance != null)
  }
}
