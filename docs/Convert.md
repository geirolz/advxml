### Convert <a name="Convert"></a>
This feature provides several conversion utility.
The main utility are:
- ValidatedConverter: convert A to ValidatedNelEx[B]  
- TextConverter: convert object to xml text
- XmlConverter: convert object to xml and viceversa
   
*XmlConverter*   
Conversion are not automatic and you need to manual map XML and Model.

In the following example if some attribute or node is missing whole conversion will fail reporting ALL
errors.
    
#### Example(XML to Model)
```scala
  import advxml.core.convert.ValidatedConverter
  import advxml.core.convert.xml.{ModelToXml, XmlToModel}
  import advxml.core.validate.ValidatedNelEx
  import cats.data.Validated.Valid
  import scala.xml.Elem

  import advxml.syntax.convert._
  import advxml.syntax.traverse.validated._
  import cats.syntax.all._
  import cats.instances.option._
  import advxml.instances.validate._

  case class Person(name: String, surname: String, age: Option[Int])

  implicit val converter: XmlToModel[Elem, Person] = ValidatedConverter.of(x => {
    (
      x \@! "Name",
      x \@! "Surname",
      x.\@?("Age").map(_.toInt).valid
    ).mapN(Person)
  })

  val xml = <Person Name="Matteo" Surname="Bianchi"/>
  val res: ValidatedNelEx[Person] = xml.as[Person]

  assert(res.map(_.name) == Valid("Matteo"))
  assert(res.map(_.surname) == Valid("Bianchi"))
```

#### Example(Model to XML) 
```scala
  import advxml.core.convert.ValidatedConverter
  import advxml.core.convert.xml.{ModelToXml, XmlToModel}
  import advxml.core.validate.ValidatedNelEx
  import cats.data.Validated.Valid
  import scala.xml.Elem

  import advxml.syntax.convert._
  import advxml.syntax.traverse.validated._
  import cats.syntax.all._
  import cats.instances.option._
  import advxml.instances.validate._

  case class Person(name: String, surname: String, age: Option[Int])

  implicit val converter: ModelToXml[Person, Elem] = ValidatedConverter.of(
    x =>
      Valid(
        <Person Name={x.name} Surname={x.surname} Age={x.age.map(_.toString).getOrElse("")}/>
      )
  )

  val p = Person("Matteo", "Bianchi", Some(23))
  val res: ValidatedNelEx[Elem] = p.as[Elem]

  assert(res == Valid(<Person Name="Matteo" Surname="Bianchi" Age="23"/>))
```