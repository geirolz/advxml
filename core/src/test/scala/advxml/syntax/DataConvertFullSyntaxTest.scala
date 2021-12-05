package advxml.syntax

import advxml.data.*
import advxml.transform.XmlZoom.$
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Elem

/** Advxml Created by geirolad on 28/06/2019.
  *
  * @author
  *   geirolad
  */
class DataConvertFullSyntaxTest extends AnyFunSuite {

  import advxml.implicits.*
  import advxml.testing.ScalacticXmlEquality.*
  import cats.data.Validated.*
  import cats.syntax.all.*

  case class Car(brand: String, model: String)
  case class Person(name: String, surname: String, age: Option[Int], note: String, cars: Seq[Car])

  test("XML to Model - Convert simple case class") {
    implicit val converter: ValidatedConverter[Elem, Person] = ValidatedConverter.of(person => {
      (
        person.attr("Name").asValidated[String],
        person.attr("Surname").asValidated[String],
        person.attr("Age").as[Option[Int]].valid,
        $(person).Note.content.asValidated[String],
        $(person).Cars.Car.run[ValidatedNelThrow].andThen { cars =>
          cars
            .map(car => {
              (
                car.attr("Brand").asValidated[String],
                car.attr("Model").asValidated[String]
              ).mapN(Car.apply)
            })
            .sequence
        }
      ).mapN(Person.apply)
    })

    val xml =
      <Person Name="Matteo" Surname="Bianchi" Age="24">
        <Note>NOTE</Note>
        <Cars>
          <Car Brand="Ferrari" Model="LaFerrari"/>
          <Car Brand="Fiat" Model="500"/>
        </Cars>
      </Person>

    val res: ValidatedNelThrow[Person] = xml.asValidated[Person]

    assert(res.map(_.name) == Valid("Matteo"))
    assert(res.map(_.surname) == Valid("Bianchi"))
    assert(res.map(_.age) == Valid(Some(24)))
    assert(res.map(_.note) == Valid("NOTE"))
    assert(res.map(_.cars) == Valid(Seq(Car("Ferrari", "LaFerrari"), Car("Fiat", "500"))))
  }

  test("Model to XML - Convert simple case class") {

    implicit val converter: ValidatedConverter[Person, Elem] = ValidatedConverter.of(person =>
      Valid(
        <Person Name={person.name} Surname={person.surname} Age={
          person.age.map(_.toString).getOrElse("")
        }>
        <Note>{person.note}</Note>
        <Cars>
          {
          person.cars.map(car => {
            <Car Brand={car.brand} Model={car.model}/>
          })
        }
        </Cars>
        </Person>
      )
    )

    val p         = Person("Matteo", "Bianchi", Some(23), "Matteo note", Seq(Car("Fiat", "500")))
    val res: Elem = p.asValidated[Elem].toOption.get

    assert(
      res ===
        <Person Name="Matteo" Surname="Bianchi" Age="23">
        <Note>Matteo note</Note> 
        <Cars>
          <Car Brand="Fiat" Model="500"/>
        </Cars>
      </Person>
    )
  }
}
