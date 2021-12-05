package advxml.data

import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.NodeSeq

class ConverterTest extends AnyFunSuite {

  test("Test Converter.id") {
    val testValue                   = "TEST"
    val converter: String As String = Converter.id
    val result: String              = converter.run(testValue)
    assert(result == testValue)
  }

  test("Test Converter.pure") {
    val testValue                = "TEST"
    val converter: Int As String = Converter.pure(testValue)

    assert(converter.run(100) == testValue)
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
    val foo                                = <foo></foo>
    val encoder: XmlDecoder[NodeSeq]       = XmlDecoder.of(ns => Valid(ns))
    val result: ValidatedNelThrow[NodeSeq] = encoder.run(foo)
    assert(result == Valid(foo))
  }

  test("Test XmlDecoder.cost") {
    val foo                                = <foo></foo>
    val bar                                = <bar></bar>
    val encoder: XmlDecoder[NodeSeq]       = XmlDecoder.pure(bar)
    val result: ValidatedNelThrow[NodeSeq] = encoder.run(foo)
    assert(result == Valid(bar))
  }

  test("Test XmlEncoder.of") {
    val foo                          = <foo></foo>
    val encoder: XmlEncoder[NodeSeq] = XmlEncoder.of(ns => ns)
    val result: NodeSeq              = encoder.run(foo)
    assert(result == foo)
  }

  test("Test XmlEncoder.pure") {
    val foo                          = <foo></foo>
    val bar                          = <bar></bar>
    val encoder: XmlEncoder[NodeSeq] = XmlEncoder.pure(bar)
    val result: NodeSeq              = encoder.run(foo)
    assert(result == bar)
  }
}
