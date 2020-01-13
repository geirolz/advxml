package advxml.syntax

import advxml.core.convert.{Converter, PureConverter, ValidatedConverter}
import advxml.core.validate.ValidatedEx
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}

class ConvertersSyntaxTest extends AnyFunSuite {

  import advxml.syntax.convert._
  import cats.instances.option._
  import cats.instances.try_._

  test("ConverterOps - mapAs | with Converter") {

    implicit val converter: Converter[Try, String, Int] =
      Converter.of(str => Try(str.toInt))

    val value: Option[String] = Some("1")
    val result: Option[Try[Int]] = value.mapAs[Try, Int]

    assert(result.get.get == 1)
  }

  test("ConverterOps - mapAs | with PureConverter") {
    implicit val converter: PureConverter[String, Int] = PureConverter.of(_.toInt)

    val value: Option[String] = Some("1")
    val result: Option[Int] = value.mapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps - flatMapAs") {

    implicit val converter: Converter[Try, String, Int] =
      Converter.of(str => Try(str.toInt))

    val value: Try[String] = Success("1")
    val result: Try[Int] = value.flatMapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps - as | with Converter") {
    implicit val converter: Converter[Try, String, Int] =
      Converter.of(str => Try(str.toInt))

    val value: String = "1"
    val result: Try[Int] = value.as[Try, Int]

    assert(result.get == 1)
  }

  test("ConverterOps - as | with PureConverter") {
    implicit val converter: PureConverter[String, Int] = PureConverter.of(_.toInt)

    val value: String = "1"
    val result: Int = value.as[Int]

    assert(result == 1)
  }

  test("ConverterOps - as | with ValidatedConverter") {
    implicit val converter: ValidatedConverter[String, Int] =
      ValidatedConverter.of(str => Valid(str.toInt))

    val value: String = "1"
    val result: ValidatedEx[Int] = value.as[Int]

    assert(result.toOption.get == 1)
  }
}
