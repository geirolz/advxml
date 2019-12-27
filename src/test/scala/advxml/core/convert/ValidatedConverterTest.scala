package advxml.core.convert

import advxml.core.validate.ValidatedEx
import cats.data.Validated.Valid
import org.scalatest.FunSuite

class ValidatedConverterTest extends FunSuite {

  test("Test ValidatedConverter.of") {
    val converter: ValidatedConverter[Int, String] = ValidatedConverter.of(int => Valid(int.toString))

    assert(converter.run(10).toOption.get == "10")
    assert(converter.run(20).toOption.get == "20")
    assert(converter.run(30).toOption.get == "30")
  }

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

  test("Test ValidatedConverter.apply") {
    implicit val iConverter: ValidatedConverter[Int, String] = ValidatedConverter.of(int => Valid(int.toString))
    val converter = ValidatedConverter[Int, String]

    assert(converter.run(10).toOption.get == "10")
    assert(converter.run(20).toOption.get == "20")
    assert(converter.run(30).toOption.get == "30")
  }
}
