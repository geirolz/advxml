# Traverse
This feature allow users read/obtain node(s) or attributes.

This feature core is written with tagless final and all methods 
returns an output value wrapped in `F[G[_]]`, where:
- `_F[_]_` is the main type, all result is wrapped in F.
- `G[_]` is the result of the read.

You can import specific syntax using *traverse* object provided by *instances*, 
inside it you can find multiple objects containing all implicits for the required syntax.

At the moment are available supports for:
- try
- either
- validatedEx(_cats ValidatedNel[Throwble]_)
     
## Syntax
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

#### Example
```scala
    import advxml.syntax.traverse.try_._
    import scala.xml._
    import scala.util.Try
    
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
    val optionalNode: Try[Option[NodeSeq]] = doc \ "Person" \? "Cars"
    
    //Nested nodes
    val mandatoryNestedNode: Try[NodeSeq] = doc \ "Person" \\! "Cars"
    val optionalNestedNode: Try[Option[NodeSeq]] = doc \ "Person" \\? "Cars"

    //Attributes
    val mandatoryAttr: Try[String] = doc \ "Person" \ "Cars" \ "Car" \@! "Brand"
    val optionalAttr: Try[Option[String]] = doc \ "Person" \ "Cars" \ "Car" \@? "Brand"

    //Text
    val mandatoryText: Try[String] = doc \ "Person" \ "Cars" \ "Car" \ "Price" !
    val optionalText: Try[Option[String]] = doc \ "Person" \ "Cars" \ "Car" \ "Price" ?
```