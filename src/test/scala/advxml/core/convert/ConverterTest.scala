package advxml.core.convert

import cats.Id
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class ConverterTest extends AnyFunSuite {

  test("Test Converter.id") {
    import cats.instances.try_._
    val testValue = "TEST"
    val converter: Converter[Try, String, String] = Converter.id
    val result: Try[String] = converter.run(testValue)

    assert(result.get == testValue)
  }

  test("Test Converter.const") {
    import cats.instances.try_._
    val testValue = "TEST"
    val converter: Converter[Try, Int, String] = Converter.const(testValue)

    assert(converter.run(100).get == testValue)
    assert(converter.run(200).get == testValue)
    assert(converter.run(300).get == testValue)
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

    assert(Converter[Try, Int, String].run(10).get == "10")
    assert(Converter[Try, Int, String].run(20).get == "20")
    assert(Converter[Try, Int, String].run(30).get == "30")
  }

  test("Test Converter.apply - using implicit unsafe Converter") {
    implicit val converter: PureConverter[Int, String] = PureConverter.of(_.toString)

    assert(Converter[Id, Int, String].run(10) == "10")
    assert(Converter[Id, Int, String].run(20) == "20")
    assert(Converter[Id, Int, String].run(30) == "30")
  }
}
