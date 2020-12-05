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
class ConverterFullSyntaxTest extends AnyFunSuite {

  import advxml.implicits._
  import advxml.testUtils.ScalacticXmlEquality._
  import cats.syntax.all._

  case class Car(brand: String, model: String)
  case class Person(name: String, surname: String, age: Option[Int], note: String, cars: Seq[Car])

  test("XML to Model - Convert simple case class") {

    implicit val converter: Elem XmlTo Person = ValidatedConverter.of(person => {
      (
        person /@ "Name",
        person /@ "Surname",
        person./@[Option]("Age").flatMapAs[Int].valid,
        $(person).Note.textM,
        $(person).Cars.Car.run.flatMap { cars =>
          cars
            .map(car => {
              (
                car /@ "Brand",
                car /@ "Model"
              ).mapN(Car)
            })
            .sequence
        }
      ).mapN(Person)
    })

    val xml =
      <Person Name="Matteo" Surname="Bianchi" Age="24">
        <Note>NOTE</Note>
        <Cars>
          <Car Brand="Ferrari" Model="LaFerrari"/>
          <Car Brand="Fiat" Model="500"/>
        </Cars>
      </Person>

    val res: ValidatedNelEx[Person] = xml.asValidated[Person]

    assert(res.map(_.name) == Valid("Matteo"))
    assert(res.map(_.surname) == Valid("Bianchi"))
    assert(res.map(_.age) == Valid(Some(24)))
    assert(res.map(_.note) == Valid("NOTE"))
    assert(res.map(_.cars) == Valid(Seq(Car("Ferrari", "LaFerrari"), Car("Fiat", "500"))))
  }

  test("Model to XML - Convert simple case class") {

    implicit val converter: Person ToXml Elem = ValidatedConverter.of(person =>
      Valid(
        <Person Name={person.name} Surname={person.surname} Age={person.age.map(_.toString).getOrElse("")}>
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

    val p = Person("Matteo", "Bianchi", Some(23), "Matteo note", Seq(Car("Fiat", "500")))
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
