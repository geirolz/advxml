package advxml.core.data

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}

class ConverterTest extends AnyFunSuite {

  test("Test Converter.id") {
    val testValue = "TEST"
    val converter: String As String = Converter.id
    val result: String = converter.run(testValue)
    assert(result == testValue)
  }

  test("Test Converter.const") {
    val testValue = "TEST"
    val converter: Int As String = Converter.const(testValue)

    assert(converter.run(100) == testValue)
    assert(converter.run(200) == testValue)
    assert(converter.run(300) == testValue)
  }

  test("Test Converter.apply - using implicit safe Converter") {
    implicit val converter: Int As Try[String] = Converter.of(int => Try(int.toString))

    assert(Converter[Int, Try[String]].run(10) == Success("10"))
    assert(Converter[Int, Try[String]].run(20) == Success("20"))
    assert(Converter[Int, Try[String]].run(30) == Success("30"))
  }

  test("Test Converter.apply - using implicit unsafe Converter") {
    implicit val converter: Int As String = Converter.of(_.toString)

    assert(Converter[Int, String].run(10) == "10")
    assert(Converter[Int, String].run(20) == "20")
    assert(Converter[Int, String].run(30) == "30")
  }
}
