## Convert <a name="Convert"></a>
Convert ecosystem is based on a simple Kleisli instance defined implicitly in the scope
and applied on an instance using the syntax that advxml offers.

In order to simplify the code advxml defines the following type aliases
```scala
type Converter[F[_], -A, B] = Kleisli[F, A, B]
type PureConverter[-A, B] = Converter[Id, A, B]
type ValidatedConverter[-A, B] = Converter[ValidatedNelEx, A, B]
```

### How to create a converter
```scala
import scala.util.Try
import advxml.core.data.{Converter, PureConverter, ValidatedConverter}
import advxml.implicits._

object MyConverters {

  implicit val converter: Converter[Try, String, Int] =
    Converter.of(str => Try(str.toInt))

  implicit val pureConverter: PureConverter[String, Int] =
    PureConverter.of(str => str.toInt)

  implicit val validatedConverter: ValidatedConverter[String, Int] =
    ValidatedConverter.of(str => Try(str.toInt).toValidatedEx)
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

//Using 'converter' of type Converter[Try, String, Int]
val resTry : Try[Int] = str.as[Try, Int]

//Using 'pureConverter' of type PureConverter[String, Int] = Converter[Id, String, Int] 
val resId : Int = str.as[Int]

//Using 'validatedConverter' of type ValidatedConverter[String, Int] = Converter[ValidatedNelEx, String, Int] 
val resValidated : ValidatedNelEx[Int] = str.asValidated[Int] 
```

Moreover, you can convert wrapped value using `mapAs` as following if an `Applicative` of the effect `F[_]` 
is available in the scope. 

```scala
import scala.util.Try
import cats.instances.try_._
import advxml.syntax.convert._
import advxml.instances.convert._

val optStr : Option[String] = Some("1")
val optInt: Option[Int] = optStr.mapAs[Int]
val optTryInt: Option[Try[Int]] = optStr.mapAs[Try, Int]
```

You can even use `flatMapAs` if a `Monad` of `F[_]` is available
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

Keep in mind that also provided `PureConverter` from String to Int, Long, BigDecimal, etc...these ARE NOT exception safe, 
so is recommended the use of `flatMapAs` in these cases.

For simplify the code advxml also defines some other type alias derived from what we have just saw.

#### XmlConverter
XmlConverter is based on the most generic converters ecosystem and allows to
transform a case class to xml and vice-versa just defining a `XmlTo` or `ToXml` instance.
To define our `XmlTo` we can use advxml `XmlZoom` and `XmlContentZoom` syntax.

```scala
type XmlTo[-I <: NodeSeq, O] = ValidatedConverter[I, O]
type ToXml[-I, O <: NodeSeq] = ValidatedConverter[I, O]
```

Once defined your instance import it in your scope and use method `asValidated` to apply the
converter on the selected instance.

Conversion are not automatic and you need to manual map XML and Model.

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
import advxml.core.data.{ToXml, ValidatedConverter, ValidatedNelEx, XmlTo}
import cats.data.Validated.Valid
import cats.syntax.all._

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
  <person Name="Matteo" Surname="Bianchi" Age="24">
    <note>NOTE</note>
    <cars>
      <car brand="Ferrari" model="LaFerrari"/>
      <car brand="Fiat" model="500"/>
    </cars>
  </person>

val res: ValidatedNelEx[Person] = xml.asValidated[Person]
```

#### Example(Model to XML)
```scala
import scala.xml.Elem
import advxml.implicits._
import advxml.core.data.{ToXml, ValidatedConverter, ValidatedNelEx, XmlTo}
import cats.data.Validated.Valid
import cats.syntax.all._

implicit val converter: Person ToXml Elem = ValidatedConverter.of(person =>
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
val res: Elem = p.asValidated[Elem]
```