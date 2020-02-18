package advxml.core.convert

import advxml.core.validate.ValidatedEx
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

class ValidatedConverterTest extends AnyFunSuite {

  test("Test ValidatedConverter.of") {
    val converter: ValidatedConverter[Int, String] = ValidatedConverter.of(int => Valid(int.toString))

    assert(converter.run(10) == Valid("10"))
    assert(converter.run(20) == Valid("20"))
    assert(converter.run(30) == Valid("30"))
  }

  test("Test ValidatedConverter.id") {
    val testValue = "TEST"
    val converter: ValidatedConverter[String, String] = ValidatedConverter.id
    val value: ValidatedEx[String] = converter.run(testValue)

    assert(value == Valid(testValue))
  }

  test("Test ValidatedConverter.const") {
    val testValue = "TEST"
    val converter: ValidatedConverter[Int, String] = ValidatedConverter.const(testValue)

    assert(converter.run(10) == Valid(testValue))
    assert(converter.run(20) == Valid(testValue))
    assert(converter.run(30) == Valid(testValue))
  }

  test("Test ValidatedConverter.apply") {
    implicit val iConverter: ValidatedConverter[Int, String] = ValidatedConverter.of(int => Valid(int.toString))
    val converter = ValidatedConverter[Int, String]

    assert(converter.run(10) == Valid("10"))
    assert(converter.run(20) == Valid("20"))
    assert(converter.run(30) == Valid("30"))
  }
}
