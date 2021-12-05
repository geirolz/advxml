## Convert <a name="Convert"></a>
Convert "ecosystem" is based on a simple Kleisli instance defined implicitly in the scope
and applied on an instance using the syntax that advxml offers.

In order to simplify the code advxml defines the following types aliases
```scala
type Converter[-A, B] = Kleisli[Id, A, B]
type As[-A, B] = Converter[A, B]
type ValidatedConverter[-A, B] = Converter[A, ValidatedNelThrow[B]]
type OptionConverter[-A, B] = Converter[A, Option[B]]
```

### How to create a converter

```scala
import scala.util.Try
import advxml.data.{Converter, ValidatedConverter, ValidatedNelThrow}
import advxml.syntax.*
import advxml.data.Converter.*

object MyConverters {

  implicit val converter: Converter[String, Try[Int]] =
    Converter.of(str => Try(str.toInt))

  implicit val converterId: Converter[String, Int] =
    Converter.of(str => str.toInt)

  implicit val validatedConverter: ValidatedConverter[String, Int] =
    ValidatedConverter.of(str => Try(str.toInt).to[ValidatedNelThrow])
}
```

Once defined and imported in our scope let's see how to use it.

### How to use a converter

```scala
import scala.util.Try
import advxml.data.ValidatedNelThrow
import advxml.syntax.*
import MyConverters.*

val str: String = "10"

//Using 'converter' of type Converter[String, Try[Int]]
val resTry: Try[Int] = str.as[Try[Int]]

//Using 'converterId' of type Converter[String, Int] = Converter[String, Int]
val resId: Int = str.as[Int]

//Using 'validatedConverter' of type ValidatedConverter[String, Int] = Converter[String, ValidatedNelThrow[Int]]
val resValidated: ValidatedNelThrow[Int] = str.asValidated[Int] 
```

Moreover, we can convert wrapped value using `mapAs` as following if an `Applicative` of the effect `F[_]` 
is available in the scope.

```scala
import advxml.data.Converter.*
import advxml.syntax.*
import cats.instances.option.*
import cats.instances.try_.*
import scala.util.Try

val optStr: Option[String] = Some("1")
val optInt: Option[Int] = optStr.flatMapAs[Int]
val optTryInt: Option[Try[Int]] = optStr.mapAs[Try[Int]]
```

We can even use `flatMapAs` if a `FlatMap` of `F[_]` is available
(or `andThenAs` for cats `Validated`)

```scala
import scala.util.{Success, Try}
import cats.instances.try_.*
import advxml.syntax.*
import advxml.data.Converter.*

val tryStr: Try[String] = Success("1")
val tryInt: Try[Int] = tryStr.flatMapAs[Int]
```

Multiple converters for standard types are already defined by advxml and you just need to import them with

```scala
import advxml.data.Converter.*
```

For simplify the code advxml also defines some other type alias derived from what we have just saw.

#### XmlConverter
XmlConverter is based on the most generic converters "ecosystem" and allows us to
transform a case class to xml and vice-versa just defining a `XmlDecoder` or `XmlEncoder` instance.

```scala
type XmlDecoder[T] = ValidatedConverter[NodeSeq, T]
type XmlEncoder[T] = ValidatedConverter[T, NodeSeq]
```

Once defined our instances, we need to import it in our scope and use `decode[T]` or `encode[T]` method to apply the
converter on the selected instance. Due `XmlDecoder` or `XmlEncoder` are `ValidatedConverter` 
we can also use `asValidated[T]` method.

Conversion are not automatic yet, we need to manual map XML and Model.

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
import advxml.core.data.{ValidatedConverter, ValidatedNelThrow, XmlDecoder}
import advxml.implicits.*
import cats.data.Validated.Valid
import cats.syntax.all.*

case class Car(brand: String, model: String)
case class Person(name: String,
                  surname: String,
                  age: Option[Int],
                  note: String,
                  cars: Seq[Car])

implicit val converter: XmlDecoder[Person] = XmlDecoder.of(person => {
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

val res: ValidatedNelThrow[Person] = xml.decode[Person]
```

#### Example(Model to XML)

```scala
import advxml.data.{ValidatedNelThrow, XmlEncoder}
import advxml.syntax.*
import cats.data.Validated.Valid

import scala.xml.NodeSeq

implicit val converter: XmlEncoder[Person] = XmlEncoder.of(person =>
  <Person Name={person.name} Surname={person.surname} Age={person.age.map(_.toString).getOrElse("")}>
    <Note>
      {person.note}
    </Note>
    <Cars>
      {person.cars.map(car => {
        <Car Brand={car.brand}/>
    })}
    </Cars>
  </Person>
)

val p = Person("Matteo", "Bianchi", Some(23), "Matteo note", Seq(Car("Fiat")))
val res: NodeSeq = p.encode
```