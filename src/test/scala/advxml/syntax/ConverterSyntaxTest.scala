package advxml.syntax

import advxml.core.data.{Converter, ValidatedConverter, ValidatedNelEx, XmlDecoder, XmlEncoder}
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.{Elem, NodeSeq}

class ConverterSyntaxTest extends AnyFunSuite {

  import advxml.syntax.convert._
  import cats.instances.option._
  import cats.instances.try_._

  test("ConverterOps - mapAs | with Converter") {

    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: Option[String] = Some("1")
    val result: Option[Try[Int]] = value.mapAs[Try[Int]]

    assert(result.get.get == 1)
  }

  test("ConverterOps - mapAs | with Converter Id") {
    implicit val converter: Converter[String, Int] = Converter.of(_.toInt)

    val value: Option[String] = Some("1")
    val result: Option[Int] = value.mapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps - flatMapAs") {

    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: Try[String] = Success("1")
    val result: Try[Int] = value.flatMapAs[Int]

    assert(result.get == 1)
  }

  test("ConverterOps - as | with Converter") {
    implicit val converter: Converter[String, Try[Int]] =
      Converter.of(str => Try(str.toInt))

    val value: String = "1"
    val result: Try[Int] = value.as[Try[Int]]

    assert(result.get == 1)
  }

  test("ConverterOps - as | with Converter Id") {
    implicit val converter: Converter[String, Int] = Converter.of(_.toInt)

    val value: String = "1"
    val result: Int = value.as[Int]

    assert(result == 1)
  }

  test("ConverterOps - as | with ValidatedConverter") {
    implicit val converter: ValidatedConverter[String, Int] =
      ValidatedConverter.of(str => Valid(str.toInt))

    val value: String = "1"
    val result: ValidatedNelEx[Int] = value.asValidated[Int]

    assert(result.toOption.get == 1)
  }

  test("XmlDecoder - decode") {
    case class Person(name: String)
    implicit val decoder: XmlDecoder[Person] =
      XmlDecoder.of(xml => Valid(Person(xml \@ "name")))

    val xml: Elem = <Person name="mimmo" />
    val result: ValidatedNelEx[Person] = xml.decode[Person]

    assert(result.toOption.get.name == "mimmo")
  }

  test("XmlEncoder - decode") {
    case class Person(name: String)
    implicit val encoder: XmlEncoder[Person] =
      XmlEncoder.of(p => Valid(<Person name={p.name} />))

    val person: Person = Person("mimmo")
    val result: ValidatedNelEx[NodeSeq] = person.encode

    assert(result.toOption.get === <Person name="mimmo" />)
  }
}
