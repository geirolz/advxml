package advxml.core

import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

class XmlConverterTest extends AnyFunSuite {

  test("XmlConverter - asModel") {

    case class Person(name: String, surname: String)

    implicit val convert: XmlToModel[Elem, Person] =
      ValidatedConverter.of(xml => Valid(Person(xml \@ "Name", xml \@ "Surname")))

    val xml = <Person Name="Mario" Surname="Bianchi"/>
    val model = XmlConverter.asModel[Elem, Person](xml)

    assert(model.map(_.name) == Valid("Mario"))
    assert(model.map(_.surname) == Valid("Bianchi"))
  }

  test("XmlConverter - asXml") {

    case class Person(name: String, surname: String)

    implicit val convert: ModelToXml[Person, Elem] =
      ValidatedConverter.of(model => Valid(<Person Name={model.name} Surname={model.surname} />))

    val model = Person("Mario", "Bianchi")
    val xml = XmlConverter.asXml[Person, Elem](model)

    assert(xml.isValid)
    assert(xml.map(_ \@ "Name") == Valid("Mario"))
    assert(xml.map(_ \@ "Surname") == Valid("Bianchi"))
  }
}
