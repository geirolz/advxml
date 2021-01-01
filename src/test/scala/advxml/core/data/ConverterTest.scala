package advxml.core.data

import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.NodeSeq

class ConverterTest extends AnyFunSuite {

  test("Test Converter.id") {
    val testValue = "TEST"
    val converter: String As String = Converter.id
    val result: String = converter.run(testValue)
    assert(result == testValue)
  }

  test("Test Converter.idF") {
    val testValue = "TEST"
    val converter: String As Option[String] = Converter.idF[Option, String]
    val result: Option[String] = converter.run(testValue)
    assert(result == Some(testValue))
  }

  test("Test Converter.const") {
    val testValue = "TEST"
    val converter: Int As String = Converter.const(testValue)

    assert(converter.run(100) == testValue)
  }

  test("Test Converter.constF") {
    val testValue = "TEST"
    val converter: Int As Option[String] = Converter.constF(testValue)

    assert(converter.run(100) == Some(testValue))
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

  test("Test XmlDecoder.of") {
    val foo = <foo></foo>
    val encoder: XmlDecoder[NodeSeq] = XmlDecoder.of(ns => Valid(ns))
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(foo))
  }

  test("Test XmlDecoder.id") {
    val foo = <foo></foo>
    val encoder: XmlDecoder[NodeSeq] = XmlDecoder.id
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(foo))
  }

  test("Test XmlDecoder.cost") {
    val foo = <foo></foo>
    val bar = <bar></bar>
    val encoder: XmlDecoder[NodeSeq] = XmlDecoder.const(bar)
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(bar))
  }

  test("Test XmlEncoder.of") {
    val foo = <foo></foo>
    val encoder: XmlEncoder[NodeSeq] = XmlEncoder.of(ns => Valid(ns))
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(foo))
  }

  test("Test XmlEncoder.id") {
    val foo = <foo></foo>
    val encoder: XmlEncoder[NodeSeq] = XmlEncoder.id
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(foo))
  }

  test("Test XmlEncoder.cost") {
    val foo = <foo></foo>
    val bar = <bar></bar>
    val encoder: XmlEncoder[NodeSeq] = XmlEncoder.const(bar)
    val result: ValidatedNelEx[NodeSeq] = encoder.run(foo)
    assert(result == Valid(bar))
  }
}
