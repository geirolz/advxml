package advxml.core.utils.internals

import org.scalatest.FunSuite

class MutableSingleUseTest extends FunSuite {

  test("SingleUse - getOrElse - unused") {
    val value = MutableSingleUse[String]("TEST")

    assert(!value.isUsed)

    val result = value.getOrElse("ALREADY_USED")

    assert(value.isUsed)
    assert(result == "TEST")
  }

  test("SingleUse - getOrElse - used") {
    val value = MutableSingleUse[String]("TEST")
    val result1 = value.getOrElse("ALREADY_USED")
    val result2 = value.getOrElse("ALREADY_USED")

    assert(value.isUsed)
    assert(result1 == "TEST")
    assert(result2 == "ALREADY_USED")
  }
}
