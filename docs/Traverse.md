# Traverse
This feature allow users read/obtain node(s) or attributes.

This feature core is written with tagless final and all methods 
returns an output value wrapped in `F[_]`.

You can import specific syntax using *traverse* object provided by *instances*, 
inside it you can find multiple objects containing all implicits for the required syntax.

At the moment are available supports for:
- try
- option
- either
- validated(_cats ValidatedNel[Throwble]_)
     
## Syntax
- _?_ means optional things
- _!_ means mandatory things

Mandatory syntax require  `MonadEx` in the scope.
Optional syntax require `Alternative` and `Monad` in the scope.   
     
So the syntax is the same of standard XML library but you can postfix *?* or *!* to method name in order to
handle the presence of what you are looking for.

*Attributes*
- _\\@?_ optional attribute
- _\\@!_ mandatory attribute

*Nodes*
- _\\?_ optional node
- _\\!_ mandatory node

*Nested nodes*
- _\\\\\?_ optional nested node
- _\\\\\!_ mandatory nested node

*Text*
- _?_ optional text
- _|?|_ optional trimmed text
- _!_ mandatory text
- _|!|_ mandatory trimmed text

#### Example
```scala
    import advxml.syntax.traverse.try_._
    import scala.xml._
    import scala.util.Try
    import cats.instances.try_._
    import cats.instances.option._

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
    val mandatoryNode: Try[NodeSeq] = doc \ "Person" \! "Cars"
    val optionalNode: Option[NodeSeq] = doc \ "Person" \? "Cars"
    
    //Nested nodes
    val mandatoryNestedNode: Try[NodeSeq] = doc \ "Person" \\! "Cars"
    val optionalNestedNode: Option[NodeSeq] = doc \ "Person" \\? "Cars"

    //Attributes
    val mandatoryAttr: Try[String] = doc \ "Person" \ "Cars" \ "Car" \@! "Brand"
    val optionalAttr: Option[String] = doc \ "Person" \ "Cars" \ "Car" \@? "Brand"

    //Text
    val mandatoryText: Try[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" !
    val optionalText: Option[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" ?

    //Trimmed Text
    val mandatoryTrimmedText: Try[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" |!|
    val optionalTrimmedText: Option[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" |?|
```

## Nested
You can even traverse wrapped `NodeSeq` with the same syntax

```scala
    import advxml.syntax.traverse.try_._
    import advxml.instances.transform._
    import scala.xml._
    import scala.util.Try
    import cats.instances.try_._
    import cats.instances.option._
    
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

    val cars: Try[NodeSeq] = doc \ "Person" \! "Cars"
    val fiatCar : Try[NodeSeq] = cars \! "Car" 
```
