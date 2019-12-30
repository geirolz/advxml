# Transform
The syntax to edit xml is very intuitive, first of all you need to define a **Modification Rule**
and then pass it as argument to `transform` method provided via extensions methods in `NodeSeq` class. 
`transform` method will return the XML edited wrapped in `F[_]`.

A modification rule is composed by:
- `Zoom`: A function with the the aim to zoom inside document and select the node to edit.
- `XmlModifier`: A function that apply a transformation over selected node.

The pseudo code for this is something like: `modifier.apply(zoom.apply(document))`

Note: Integrated with `AdvXml` there are implicits in order to add a more fluent syntax for rule creation.
Each example is written with fluent syntax using implicit but commented you can see the "Desugared" version.
 
#### Classes
- **XmlModifier** = Object that represent an XML modification, is a function like `NodeSeq => F[NodeSeq]`.
   - *ComposableXmlModifier* = Modifier that can be combined with other `ComposableXmlModifier`.
   - *FinalXmlModifier* = Modifier that can not be combine with other `XmlModifier`, for example `Remove`.
- **Zoom** = Type alias to `NodeSeq => NodeSeq` used to zoom on specific node.
- **PartialXmlRule** = An incomplete `XmlRule` so it has the `Zoom` instance but no the `XmlModifier`.
- **XmlRule** = An object that contains `Zoom` instance and `XmlModifier`, this class provides a method to create the 
scala xml `RewriteRule`.

#### Syntax
- **$** create a partial rule.
- **==>** is an alias to `withModifier` method.
 
#### Example
```scala

    //Scala imports
    import scala.xml.Elem
    import scala.xml.NodeSeq
    import scala.util.Try

    //import MonadError instance for Try
    import cats.instances.try_._
    
    //Advxml imports
    import advxml.core.transform.XmlRule
    //Supply $ and ==> syntax
    import advxml.syntax.transform._
    //Supply Append class
    import advxml.instances.transform._

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

## Multiple modifiers
If you need apply more that one modification on 
a selected node you can combine actions calling again `withModifier` method.

#### Example
```scala
    import advxml.implicits._
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
          ==> Append(<Car Brand="Lamborghini"/>)
          ==> Append(<Car Brand="Ferrari"/>)
          ==> Append(<Car Brand="Bmw"/>)

//  Desugared
//  val rules: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
//      .withModifier(Append(<Car Brand="Lamborghini"/>))
//      .withModifier(Append(<Car Brand="Ferrari"/>))
//      .withModifier(Append(<Car Brand="Bmw"/>))
        
    val result: Try[NodeSeq] = doc.transform[Try](rules)  
```

## Root transformation
If you need to edit the document root you can use `root` as zoom action.
`root` value is provided by `advxml.instances.transform._` 

 *Example*
```scala
    import advxml.implicits._
    import scala.xml._
    import scala.util._
    
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = <Root/>
    val result: Try[NodeSeq] = doc.transform[Try](root => SetAttrs("Attr1" := "TEST"))
```