package advxml.core.convert

import cats.Id
import cats.data.Kleisli
import org.scalatest.FunSuite

import scala.util.Try

class ConverterTest extends FunSuite {

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
    val converter: UnsafeConverter[String, String] = Converter.unsafeId
    val result: Id[String] = converter.run(testValue)
    assert(result == testValue)
  }

  test("Test Converter.unsafeConst") {
    val testValue = "TEST"
    val converter: UnsafeConverter[Int, String] = Converter.unsafeConst(testValue)

    assert(converter.run(100) == testValue)
    assert(converter.run(200) == testValue)
    assert(converter.run(300) == testValue)
  }

  test("Test Converter.apply - using implicit safe Converter") {
    implicit val converter: Converter[Try, Int, String] = Kleisli(int => Try(int.toString))

    assert(Converter(10).get == "10")
    assert(Converter(20).get == "20")
    assert(Converter(30).get == "30")
  }

  test("Test Converter.apply - using implicit unsafe Converter") {
    implicit val converter: UnsafeConverter[Int, String] = Kleisli[Id, Int, String](_.toString)

    assert(Converter(10) == "10")
    assert(Converter(20) == "20")
    assert(Converter(30) == "30")
  }
}
