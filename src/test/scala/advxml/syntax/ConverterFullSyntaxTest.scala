package advxml.syntax

import advxml.core.data.{ToXml, ValidatedConverter, ValidatedNelEx, XmlTo}
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

/** Advxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
//TODO: Check duplication into advxml.core.xml.XmlConverterTest
class ConverterFullSyntaxTest extends AnyFunSuite {

  import advxml.instances._
  import advxml.instances.convert._
  import advxml.instances.validated._

  import advxml.syntax.convert._
  import advxml.syntax.transform._
  import cats.syntax.all._

  test("XML to Model - Convert simple case class") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: Elem XmlTo Person = ValidatedConverter.of(x => {
      (
        x./@[ValidatedNelEx]("Name"),
        x./@[ValidatedNelEx]("Surname"),
        x./@[Option, Int]("Age").valid
      ).mapN(Person)
    })

    val xml = <Person Name="Matteo" Surname="Bianchi" Age="24"/>
    val res: ValidatedNelEx[Person] = xml.as[Person]

    assert(res.map(_.name) == Valid("Matteo"))
    assert(res.map(_.surname) == Valid("Bianchi"))
    assert(res.map(_.age) == Valid(Some(24)))
  }

  test("Model to XML - Convert simple case class") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: Person ToXml Elem = ValidatedConverter.of(x =>
      Valid(
        <Person Name={x.name} Surname={x.surname} Age={x.age.map(_.toString).getOrElse("")}/>
      )
    )

    val p = Person("Matteo", "Bianchi", Some(23))
    val res: ValidatedNelEx[Elem] = p.as[Elem]

    assert(res == Valid(<Person Name="Matteo" Surname="Bianchi" Age="23"/>))
  }
}
