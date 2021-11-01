package advxml.core.data

import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

class XmlEncoderDecoderTest extends AnyFunSuite {

  test("XmlDecoder - apply") {

    case class Person(name: String, surname: String)

    implicit val convert: XmlDecoder[Person] =
      XmlDecoder.of(xml => Valid(Person(xml \@ "Name", xml \@ "Surname")))

    val xml   = <Person Name="Mario" Surname="Bianchi"/>
    val model = XmlDecoder[Person].run(xml)

    assert(model.map(_.name) == Valid("Mario"))
    assert(model.map(_.surname) == Valid("Bianchi"))
  }

  test("XmlEncoder - apply") {

    case class Person(name: String, surname: String)

    implicit val convert: XmlEncoder[Person] =
      XmlEncoder.of(model => Valid(<Person Name={model.name} Surname={model.surname} />))

    val model: Person = Person("Mario", "Bianchi")
    val xml           = XmlEncoder[Person].run(model)

    assert(xml.isValid)
    assert(xml.map(_ \@ "Name") == Valid("Mario"))
    assert(xml.map(_ \@ "Surname") == Valid("Bianchi"))
  }
}
