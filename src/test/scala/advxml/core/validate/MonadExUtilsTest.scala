package advxml.core.validate

import org.scalatest.FunSuite

import scala.util.Try

object MonadExUtilsTest extends FunSuite {

  test("Test get implicit MonadEx instance using apply method") {
    import cats.instances.try_._
    val monadExInstance = MonadEx[Try]
    assert(monadExInstance != null)
  }

  test("Test get implicit MonadNelEx instance using apply method") {
    import advxml.instances.validate._
    val monadNelExInstance = MonadNelEx[ValidatedEx]
    assert(monadNelExInstance != null)
  }
}
