package advxml.core

import advxml.core.data.ValidatedNelEx
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class MonadExUtilsTest extends AnyFunSuite {

  test("implicit MonadEx.apply[Try]") {
    import cats.instances.try_._
    val monadExInstance = MonadEx[Try]
    assert(monadExInstance != null)
  }

  test("implicit MonadEx.apply[ValidatedNelEx]") {
    import advxml.instances.validated._
    val monadNelExInstance = MonadEx[ValidatedNelEx]
    assert(monadNelExInstance != null)
  }
}
