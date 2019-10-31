# Advxml
[![Build Status](https://travis-ci.org/geirolz/advxml.svg?branch=master)](https://travis-ci.org/geirolz/advxml)
[![codecov](https://codecov.io/gh/geirolz/advxml/branch/master/graph/badge.svg)](https://codecov.io/gh/geirolz/advxml)
[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.geirolz/advxml_2.13?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/com.github.geirolz/advxml)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![GitHub license](https://img.shields.io/github/license/geirolz/advxml)](https://github.com/geirolz/advxml/blob/master/LICENSE)

A Scala library to edit xml using native scala xml library and cats core.

## How to import

Supported Scala 2.12 and 2.13

Maven for 2.12
```
<dependency>
    <groupId>com.github.geirolz</groupId>
    <artifactId>advxml_2.12</artifactId>
    <version>0.1.5</version>
</dependency>
```

Maven for 2.13
```
<dependency>
    <groupId>com.github.geirolz</groupId>
    <artifactId>advxml_2.13</artifactId>
    <version>0.1.5</version>
</dependency>
```

Sbt
```
libraryDependencies += "com.github.geirolz" %% "advxml" % "0.1.5"
```

## Structure
The idea behind this library is offer a fluent syntax to edit and read xml.

*Features:*
- [Transformation](#Transformation)(Append, Remove, Replace, SetAttrs, RemoveAttrs)
- [Traverse](#Traverse)(read node/attributes mandatory or optional, based on Cats [ValidatedNel](https://typelevel.org/cats/datatypes/validated.html))
- [Convert](#Convert) to Model and vice versa(Based on Cats [ValidatedNel](https://typelevel.org/cats/datatypes/validated.html))
- [Normalize](#Normalize)(remove white spaces and collapse empty nodes)


### Transformation <a name="Transformation"></a>
 The syntax to edit xml is very intuitive, first of all you need to define a Modification Rule 
 and then pass it as argument to `transform` method provided via extensions methods in `NodeSeq` class. 
 `transform` method will return the XML edited.
 
 A modification rule is composed by a `Zoom` function and `XmlModifier`, the first one has the aim to zoom inside document
 and select the node to edit, the second one apply a transformation over selected node.
 
 The pseudo code for this is something like: `modifier.apply(zoom.apply(document))`
 
 Note: Integrated with `AdvXml` there are implicits in order to add a more fluent syntax for rule creation.
 Each example is written with fluent syntax using implicit but commented you can see the "Desugared" version.
 
 _Actors_
 - **XmlModifier** = Object that represent an XML modification, is a function like `NodeSeq => F[NodeSeq]` 
    - *ComposableXmlModifier* = Modifier that can be combined with other `ComposableXmlModifier`
    - *FinalXmlModifier* = Modifier that can not be combine with other `XmlModifier`, for example `Remove`
 - **Zoom** = Type alias to `NodeSeq => NodeSeq` used to zoom on specific node
 - **PartialXmlRule** = An incomplete `XmlRule` so it has the `Zoom` instance but no the `XmlModifier`
 - **XmlRule** = An object that contains `Zoom` instance and `XmlModifier`, this class provides a method to create the 
 scala xml `RewriteRule`
 
 _In a nutshell_
 - $ create a partial rule
 - ==> is an alias to `withModifier` method
 
 *Example*
```scala
    import com.github.geirolz.advxml.all._
    import com.github.geirolz.advxml.transform._
    import scala.xml._
    import scala.util._
    
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = 
    <Persons>
      <Person Name="Mimmo">
        <Cars>
          <Car Brand="Fiat"/>
        </Cars>
      </Person>
    </Persons>
    
    //Example with sugar
    val rule: XmlRule = $(_ \ "Person" \ "Cars") ==> Append(<Car Brand="Lamborghini"/>) 

//  Desugared
//  val rule: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
//      .withModifier(Append(<Car Brand="Lamborghini"/>))

    val result: Try[NodeSeq] = doc.transform[Try](rule)
```

#### Multiple modifiers
If you need apply more that one modification on a selected node you can combine actions calling again `withModifier` method.

 *Example*
```scala
    import com.github.geirolz.advxml.all._
    import com.github.geirolz.advxml.transform._
    import scala.xml._
    import scala.util._
   
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = 
    <Persons>
      <Person Name="Mimmo">
        <Cars>
          <Car Brand="Fiat"/>
        </Cars>
      </Person>
    </Persons>
    
    //you can use postfixOps and remove dots and useless brackets 
    val rules = $(_ \ "Person" \ "Cars") 
      .==>(Append(<Car Brand="Lamborghini"/>))
      .==>(Append(<Car Brand="Ferrari"/>)) 
      .==>(Append(<Car Brand="Bmw"/>))

//  Desugared
//  val rules: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
//      .withModifier(Append(<Car Brand="Lamborghini"/>))
//      .withModifier(Append(<Car Brand="Ferrari"/>))
//      .withModifier(Append(<Car Brand="Bmw"/>))
        
    val result: Try[NodeSeq] = doc.transform[Try](rules)  
```

#### Combine modifiers
You can combine multiple modifiers using `andThen` method or with syntax sugar `++`

 *Example*
```scala
    import com.github.geirolz.advxml.all._
    import com.github.geirolz.advxml.transform._
    import scala.xml._
    import scala.util._
    
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = 
    <Persons>
      <Person Name="Mimmo">
        <Cars>
          <Car Brand="Fiat"/>
        </Cars>
      </Person>
    </Persons>
    
    val rules = $(_ \ "Person" \ "Cars") ==> (
      Append(<Car Brand="Lamborghini"/>) ++ Append(<Car Brand="Ferrari"/>) ++ Append(<Car Brand="Bmw"/>)
    )

//  Desugared
//  val rules: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
//      .withModifier(Append(<Car Brand="Lamborghini"/>)
//        .andThen(Append(<Car Brand="Ferrari"/>)
//        .andThen(Append(<Car Brand="Bmw"/>))))
        
    val result: Try[NodeSeq] = doc.transform[Try](rules)  
```

#### Root transformation
If you need to edit the document root you can invoke `transform` method passing directly 
the `XmlModifier` and no the `XmlRule` instance, this means no zooming actions.

 *Example*
```scala
    import com.github.geirolz.advxml.all._
    import scala.xml._
    import scala.util._
    
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = <Root/>
    val result: Try[NodeSeq] = doc.transform[Try](SetAttrs("Attr1" -> "TEST"))
```

### Traverse <a name="Traverse"></a>
This feature allow users read/obtain node(s) 
or attributes that are, for domain, marked as _Mandatory_ or _Optional_.

In order to integrate this feature with Parsing feature the results of Traverse operations
returns a _ValidatedNel_ object(from cats), this permits a safer parsing with better error messages in case of parsing error.<br>
_Read more about [ValidatedNel](https://typelevel.org/cats/datatypes/validated.html)._

In a nutshell:
- _?_ means optional things
- _!_ means mandatory things

So the syntax is the same of standard XML library but you can postfix *?* or *!* to method name in order to
handle the presence of what you are looking for.

 *Attributes*
- _\\@?_ optional attribute
- _\\@!_ mandatory attribute

 *Nodes*
- _\\?_ optional node
- _\\!_ mandatory node

 *Nested nodes*
- _\\\?_ optional nested node
- _\\\!_ mandatory nested node

 *Text*
- _?_ optional text
- _!_ mandatory text


 *Example*
```scala
    import com.github.geirolz.advxml.validate.ValidatedEx
    import com.github.geirolz.advxml.all._
    import scala.xml._
    
    val doc: Elem = 
    <Persons>
      <Person Name="Mimmo">
        <Cars>
          <Car Brand="Fiat">
            <Price>10000€</Price>
          </Car>
        </Cars>
      </Person>
    </Persons>

    //Nodes
    val mandatoryNode: ValidatedEx[NodeSeq] = doc \ "Person" \! "Cars"
    val optionalNode: ValidatedEx[Option[NodeSeq]] = doc \ "Person" \? "Cars"
    
    //Nested nodes
    val mandatoryNestedNode: ValidatedEx[NodeSeq] = doc \ "Person" \\! "Cars"
    val optionalNestedNode: ValidatedEx[Option[NodeSeq]] = doc \ "Person" \\? "Cars"

    //Attributes
    val mandatoryAttr: ValidatedEx[String] = doc \ "Person" \ "Cars" \ "Car" \@! "Brand"
    val optionalAttr: ValidatedEx[Option[String]] = doc \ "Person" \ "Cars" \ "Car" \@? "Brand"

    //Text
    val mandatoryText: ValidatedEx[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" !
    val optionalText: ValidatedEx[Option[String]] = doc \ "Person" \ "Cars" \ "Car" \ "Price" ?
```
  
### Convert <a name="Convert"></a>
Conversion is not automatic and you need to manual map XML and Model.

In the following example if some attribute or node is missing whole conversion will fail reporting ALL
errors.
    
 *Example XML to Model*
```scala
    import com.github.geirolz.advxml.all._
    import com.github.geirolz.advxml.validate.ValidatedEx
    import com.github.geirolz.advxml.convert.impls.XmlConverter.XmlToModel
    import scala.xml._
    import cats.implicits._

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: XmlToModel[ValidatedEx, Elem, Person] = x =>
      (
        x \@! "Name",
        x \@! "Surname",
        (x \@? "Age").map(_.toInt).validNel
      ).mapN(Person)

    val xml = <Person Name="Matteo" Surname="Bianchi"/>
    val res: ValidatedEx[Person] = xml.as[Person]
```

 *Example Model to XML*
```scala
    import com.github.geirolz.advxml.all._
    import com.github.geirolz.advxml.convert.impls.XmlConverter.ModelToXml
    import com.github.geirolz.advxml.validate.ValidatedEx
    import scala.xml._
    import cats.implicits._
    import cats.data.Validated.Valid

    case class Person(name: String, surname: String, age: Option[Int])
    
    implicit val converter: ModelToXml[ValidatedEx, Person, Elem] = x =>
      Valid {
        <Person Name={x.name} Surname={x.surname} Age={x.age.map(_.toString).getOrElse("")}/>
      }
    
    val p = Person("Matteo", "Bianchi", Some(23))
    val res: ValidatedEx[Elem] = p.asXml
```

### Normalize <a name="Normalize"></a>
_Work in progress_
 
