package advxml.syntax

import advxml.core.data.{
  Converter,
  EitherThrow,
  OptionConverter,
  ValidatedConverter,
  ValidatedNelThrow,
  ValidatedThrow,
  XmlDecoder,
  XmlEncoder
}
import cats.data.Validated
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.{Elem, NodeSeq}

class DataConvertSyntaxTest extends AnyFunSuite {

  import advxml.syntax.data._
  import advxml.instances.data.convert._
  import cats.instances.option._
  import cats.instances.try_._

  test("ConverterOps.AnyFunctionK") {
    val rvalue: EitherThrow[Int] = Right(1)
    val lvalue: EitherThrow[Int] = Left(new RuntimeException(""))
    val rresult: Try[Int]        = rvalue.to[Try]
    val lresult: Try[Int]        = lvalue.to[Try]

    assert(rresult == Success(1))
    assert(lresult.isFailure)
  }

  test("ConverterOps.OptionFunctionK") {

    val svalue: Option[Int] = Some(1)
    val nvalue: Option[Int] = None
    val sresult: Try[Int]   = svalue.to[Try](new RuntimeException("ERROR"))
    val nresult: Try[Int]   = nvalue.to[Try](new RuntimeException("ERROR"))

    assert(sresult == Success(1))
    assert(nresult.isFailure)
  }

  test("ConverterOps.Applicative - mapAs | with Converter") {

    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: Option[String]    = Some("1")
    val result: Option[Try[Int]] = value.mapAs[Try[Int]]

    assert(result.get.get == 1)
  }

  test("ConverterOps.Applicative - mapAs | with Converter Id") {
    implicit val converter: Converter[String, Int] = Converter.of(_.toInt)

    val value: Option[String] = Some("1")
    val result: Option[Int]   = value.mapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps.FlatMap - flatMapAs") {

    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: Try[String] = Success("1")
    val result: Try[Int]   = value.flatMapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps.Validated - andThenAs") {

    implicit val converter: Converter[String, ValidatedThrow[Int]] =
      Converter.of(str => Validated.fromTry(Try(str.toInt)))

    val value: ValidatedThrow[String] = Valid("1")
    val result: ValidatedThrow[Int]   = value.andThenAs[Int]

    assert(result.getOrElse(-1) == 1)
  }

  test("ConverterOps.Any - as | with Converter") {
    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: String    = "1"
    val result: Try[Int] = value.as[Try[Int]]

    assert(result.get == 1)
  }

  test("ConverterOps.Any - as | with Converter Id") {
    implicit val converter: Converter[String, Int] = Converter.of(_.toInt)

    val value: String = "1"
    val result: Int   = value.as[Int]

    assert(result == 1)
  }

  test("ConverterOps.Any - asValidated") {
    implicit val converter: ValidatedConverter[String, Int] =
      ValidatedConverter.of(str => Valid(str.toInt))

    val value: String                  = "1"
    val result: ValidatedNelThrow[Int] = value.asValidated[Int]

    assert(result.toOption.get == 1)
  }

  test("ConverterOps.Any - asOption") {
    implicit val converter: OptionConverter[String, Int] =
      OptionConverter.of(str => Try(str.toInt).toOption)

    val value: String       = "1"
    val result: Option[Int] = value.asOption[Int]

    assert(result.get == 1)
  }

  test("ConverterOps.XmlDecoder - decode") {
    case class Person(name: String)
    implicit val decoder: XmlDecoder[Person] =
      XmlDecoder.of(xml => Valid(Person(xml \@ "name")))

    val xml: Elem                         = <Person name="mimmo" />
    val result: ValidatedNelThrow[Person] = xml.decode[Person]

    assert(result.toOption.get.name == "mimmo")
  }

  test("ConverterOps.XmlEncoder - decode") {
    case class Person(name: String)
    implicit val encoder: XmlEncoder[Person] =
      XmlEncoder.of(p => Valid(<Person name={p.name} />))

    val person: Person                     = Person("mimmo")
    val result: ValidatedNelThrow[NodeSeq] = person.encode

    assert(result.toOption.get === <Person name="mimmo" />)
  }
}
