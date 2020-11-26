package advxml.core.validate

import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class MonadExUtilsTest extends AnyFunSuite {

  test("Test get implicit MonadEx instance using apply method") {
    val monadExInstance = MonadEx[Try]
    assert(monadExInstance != null)
  }

  test("Test get implicit MonadNelEx instance using apply method") {
    import advxml.instances.validate._
    val monadNelExInstance = MonadNelEx[ValidatedNelEx]
    assert(monadNelExInstance != null)
  }
}
