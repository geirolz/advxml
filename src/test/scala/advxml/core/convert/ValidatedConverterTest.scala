package advxml.core.convert

import advxml.core.validate.ValidatedEx
import org.scalatest.FunSuite

class ValidatedConverterTest extends FunSuite {

  test("Test ValidatedConverter.id") {
    val testValue = "TEST"
    val converter: ValidatedConverter[String, String] = ValidatedConverter.id
    val value: ValidatedEx[String] = converter.run(testValue)

    assert(value.isValid)
    assert(value.toOption.get == testValue)
  }

  test("Test ValidatedConverter.const") {
    val testValue = "TEST"
    val converter: ValidatedConverter[Int, String] = ValidatedConverter.const(testValue)

    assert(converter.run(10).toOption.get == testValue)
    assert(converter.run(20).toOption.get == testValue)
    assert(converter.run(30).toOption.get == testValue)
  }
}
