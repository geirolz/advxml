package advxml.core.convert

import cats.Id
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}

class ConverterTest extends AnyFunSuite {

  test("Test Converter.id") {
    val testValue = "TEST"
    val converter: Converter[Try, String, String] = Converter.id
    val result: Try[String] = converter.run(testValue)

    assert(result == Success(testValue))
  }

  test("Test Converter.const") {
    val testValue = "TEST"
    val converter: Converter[Try, Int, String] = Converter.const(testValue)

    assert(converter.run(100) == Success(testValue))
    assert(converter.run(200) == Success(testValue))
    assert(converter.run(300) == Success(testValue))
  }

  test("Test Converter.unsafeId") {
    val testValue = "TEST"
    val converter: PureConverter[String, String] = PureConverter.id
    val result: Id[String] = converter.run(testValue)
    assert(result == testValue)
  }

  test("Test Converter.unsafeConst") {
    val testValue = "TEST"
    val converter: PureConverter[Int, String] = PureConverter.const(testValue)

    assert(converter.run(100) == testValue)
    assert(converter.run(200) == testValue)
    assert(converter.run(300) == testValue)
  }

  test("Test Converter.apply - using implicit safe Converter") {
    implicit val converter: Converter[Try, Int, String] = Converter.of(int => Try(int.toString))

    assert(Converter[Try, Int, String].run(10) == Success("10"))
    assert(Converter[Try, Int, String].run(20) == Success("20"))
    assert(Converter[Try, Int, String].run(30) == Success("30"))
  }

  test("Test Converter.apply - using implicit unsafe Converter") {
    implicit val converter: PureConverter[Int, String] = PureConverter.of(_.toString)

    assert(Converter[Id, Int, String].run(10) == "10")
    assert(Converter[Id, Int, String].run(20) == "20")
    assert(Converter[Id, Int, String].run(30) == "30")
  }
}
