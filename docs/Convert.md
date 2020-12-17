## Convert <a name="Convert"></a>
Convert "ecosystem" is based on a simple Kleisli instance defined implicitly in the scope
and applied on an instance using the syntax that advxml offers.

In order to simplify the code advxml defines the following types aliases
```scala
type Converter[-A, B] = Kleisli[Id, A, B]
type As[-A, B] = Converter[A, B]
type ValidatedConverter[-A, B] = Converter[A, ValidatedNelEx[B]]
type OptionConverter[-A, B] = Converter[A, Option[B]]
```

### How to create a converter

```scala
import scala.util.Try
import advxml.core.data.{Converter, ValidatedConverter}
import advxml.implicits._

object MyConverters {

  implicit val converter: Converter[String, Try[Int]] =
    Converter.of(str => Try(str.toInt))

  implicit val converterId: Converter[String, Int] =
    Converter.of(str => str.toInt)

  implicit val validatedConverter: ValidatedConverter[String, Int] =
    ValidatedConverter.of(str => Try(str.toInt).toValidatedNelEx)
}

```

Once defined and imported in our scope let's see how to use it.

### How to use a converter
```scala
import scala.util.Try
import advxml.core.data.ValidatedNelEx
import advxml.syntax.convert._
import MyConverters._

val str : String = "10"

//Using 'converter' of type Converter[String, Try[Int]]
val resTry : Try[Int] = str.as[Try[Int]]

//Using 'converterId' of type Converter[String, Int] = Converter[String, Int] 
val resId : Int = str.as[Int]

//Using 'validatedConverter' of type ValidatedConverter[String, Int] = Converter[String, ValidatedNelEx[Int]] 
val resValidated : ValidatedNelEx[Int] = str.asValidated[Int] 
```

Moreover, we can convert wrapped value using `mapAs` as following if an `Applicative` of the effect `F[_]` 
is available in the scope. 

```scala
import scala.util.Try
import cats.instances.try_._
import advxml.syntax.convert._
import advxml.instances.convert._

val optStr : Option[String] = Some("1")
val optInt: Option[Int] = optStr.mapAs[Int]
val optTryInt: Option[Try[Int]] = optStr.mapAs[Try[Int]]
```

We can even use `flatMapAs` if a `FlatMap` of `F[_]` is available
(or `andThenAs` for cats `Validated`)

```scala
import scala.util.{Success, Try}
import cats.instances.try_._
import advxml.syntax.convert._
import advxml.instances.convert._

val tryStr : Try[String] = Success("1")
val tryInt: Try[Int] = tryStr.flatMapAs[Int]
```

Multiple converters for standard types are already defined by advxml and you just need to import them with 
```scala
import advxml.instances.convert._
```

For simplify the code advxml also defines some other type alias derived from what we have just saw.

#### XmlConverter
XmlConverter is based on the most generic converters "ecosystem" and allows us to
transform a case class to xml and vice-versa just defining a `XmlTo` or `ToXml` instance.
To define our `XmlTo` we can use advxml `XmlZoom` and `XmlContentZoom` syntax.

```scala
type XmlDecoder[T] = ValidatedConverter[NodeSeq, T]
type XmlEncoder[T] = ValidatedConverter[T, NodeSeq]
```

Once defined our instances, we need to import it in out scope and use `asValidated` method to apply the
converter on the selected instance.

Conversion are not automatic and we need to manual map XML and Model.

In the following example if some attribute or node is missing whole conversion will fail reporting ALL
errors.

Let's write our model definition using case class.
```scala
case class Car(brand: String)
case class Person(name: String,
                  surname: String,
                  age: Option[Int],
                  note: String,
                  cars: Seq[Car])
```    

#### Example(XML to Model)
```scala
import scala.xml.Elem
import advxml.implicits._
import advxml.core.data.{ValidatedConverter, ValidatedNelEx, XmlDecoder}
import cats.data.Validated.Valid
import cats.syntax.all._

implicit val converter: XmlDecoder[Person] = XmlDecoder.of(person => {
  (
    person.attr("Name").asValidated[String],
    person.attr("Surname").asValidated[String],
    person.attr("Age").as[Option[Int]].valid,
    $(person).Note.content.asValidated[String],
    $(person).Cars.Car.run[ValidatedNelEx].andThen { cars =>
      cars
        .map(car => {
          (
            car.attr("Brand").asValidated[String],
            car.attr("Model").asValidated[String]
            ).mapN(Car)
        })
        .sequence
    }
    ).mapN(Person)
})

val xml =
  <person Name="Matteo" Surname="Bianchi" Age="24">
    <note>NOTE</note>
    <cars>
      <car brand="Ferrari" model="LaFerrari"/>
      <car brand="Fiat" model="500"/>
    </cars>
  </person>

val res: ValidatedNelEx[Person] = xml.decode[Person]
```

#### Example(Model to XML)
```scala
import scala.xml.Elem
import advxml.implicits._
import advxml.core.data.{ValidatedConverter, ValidatedNelEx, XmlEncoder}
import cats.data.Validated.Valid
import cats.syntax.all._

implicit val converter: XmlEncoder[Person] = XmlEncoder.of(person =>
  Valid(
    <Person Name={person.name} Surname={person.surname} Age={person.age.map(_.toString).getOrElse("")}>
      <Note>{person.note}</Note>
      <Cars>
        {person.cars.map(car => {
            <Car Brand={car.brand} Model={car.model}/>
        })}
      </Cars>
    </Person>
  )
)

val p = Person("Matteo", "Bianchi", Some(23), "Matteo note", Seq(Car("Fiat", "500")))
val res: Elem = p.encode[Elem]
```