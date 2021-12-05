## Convert <a name="Convert"></a>
Convert "ecosystem" is based on `Convert` type-class implicitly in the scope
and applied on an instance using the syntax that advxml provides.

In order to simplify the code advxml defines the following types aliases
```scala
import advxml.data.Converter
import advxml.data.ValidatedNelThrow

type As[-A, B] = Converter[A, B]
type ValidatedConverter[-A, B] = Converter[A, ValidatedNelThrow[B]]
type OptionConverter[-A, B] = Converter[A, Option[B]]
```

### How to create a converter

```scala
import scala.util.Try
import advxml.data.{Converter, ValidatedConverter, ValidatedNelThrow}
import advxml.implicits.*
import advxml.data.Converter.*
import cats.instances.try_.*
  
object MyConverters {

  implicit val converter: Converter[String, Try[Int]] =
    Converter.of(str => Try(str.toInt))

  implicit val converterId: Converter[String, Int] =
    Converter.of(str => str.toInt)

  implicit val validatedConverter: ValidatedConverter[String, Int] =
    ValidatedConverter.of(str => ValidatedNelThrow.fromTry(Try(str.toInt)))
}
```

Once defined and imported in our scope let's see how to use it.

### How to use a converter

```scala
import scala.util.Try
import advxml.syntax.*
import MyConverters.*

val str: String = "10"
// str: String = "10"

//Using 'converter' of type Converter[String, Try[Int]]
val resTry: Try[Int] = str.as[Try[Int]]
// resTry: Try[Int] = Success(value = 10)

//Using 'converterId' of type Converter[String, Int] = Converter[String, Int]
val resId: Int = str.as[Int]
// resId: Int = 10

//Using 'validatedConverter' of type ValidatedConverter[String, Int] = Converter[String, ValidatedNelThrow[Int]]
val resValidated: ValidatedNelThrow[Int] = str.asValidated[Int] 
// resValidated: ValidatedNelThrow[Int] = Valid(a = 10)
```

Moreover, we can convert wrapped value using `mapAs` as following if an `Applicative` of the effect `F[_]` 
is available in the scope.

```scala
import advxml.syntax.*
import cats.instances.option.*
import cats.instances.try_.*
import scala.util.Try

val optStr: Option[String] = Some("1")
// optStr: Option[String] = Some(value = "1")
val optInt: Option[Int] = optStr.flatMapAs[Int]
// optInt: Option[Int] = Some(value = 1)
val optTryInt: Option[Try[Int]] = optStr.mapAs[Try[Int]]
// optTryInt: Option[Try[Int]] = Some(value = Success(value = 1))
```

We can even use `flatMapAs` if a `FlatMap` of `F[_]` is available
(or `andThenAs` for cats `Validated`)

```scala modc
val tryStr: Try[String] = Success("1")
val tryInt: Try[Int] = tryStr.flatMapAs[Int]
```

Multiple converters for standard types are already defined by advxml and you just need to import them with

```scala modc:silent:reset
import advxml.data.Converter.*
```

For simplify the code advxml also defines some other type alias derived from what we have just saw.

#### XmlConverter
XmlConverter is based on the most generic converters "ecosystem" and allows us to
transform a case class to xml and vice-versa just defining a `XmlDecoder` or `XmlEncoder` instance.

```scala modc:reset:silent
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
```scala modc:silent
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
import advxml.data.*
import advxml.transform.XmlZoom.*
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
// converter: XmlDecoder[Person] = advxml.data.Converter$$anonfun$of$2@3d4f0757

val xml =
  <Person Name="Matteo" Surname="Bianchi" Age="24">
    <Note>NOTE</Note>
    <Cars>
      <Car Brand="Ferrari" Model="LaFerrari"/>
      <Car Brand="Fiat" Model="500"/>
    </Cars>
  </Person>
// xml: Elem = <Person Name="Matteo" Surname="Bianchi" Age="24">
//     <Note>NOTE</Note>
//     <Cars>
//       <Car Brand="Ferrari" Model="LaFerrari"/>
//       <Car Brand="Fiat" Model="500"/>
//     </Cars>
//   </Person>

val res: ValidatedNelThrow[Person] = xml.decode[Person]
// res: ValidatedNelThrow[Person] = Valid(Person(Matteo,Bianchi,Some(24),NOTE,Vector(Car(Ferrari,LaFerrari), Car(Fiat,500))))
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
// converter: XmlEncoder[Person] = advxml.data.Converter$$anonfun$of$2@c4097ac

val p = Person("Matteo", "Bianchi", Some(23), "Matteo note", Seq(Car("Fiat", "500")))
// p: Person = Person(Matteo,Bianchi,Some(23),Matteo note,List(Car(Fiat,500)))
val res: NodeSeq = p.encode
// res: NodeSeq = <Person Name="Matteo" Surname="Bianchi" Age="23">
//     <Note>
//       Matteo note
//     </Note>
//     <Cars>
//       <Car Brand="Fiat"/>
//     </Cars>
//   </Person>
```