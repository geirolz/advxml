# Advxml
[![Build Status](https://travis-ci.org/geirolz/advxml.svg?branch=master)](https://travis-ci.org/geirolz/advxml)
[![codecov](https://codecov.io/gh/DavidGeirola/advxml/branch/master/graph/badge.svg)](https://codecov.io/gh/DavidGeirola/advxml)

A Scala library to edit xml using native scala xml library.

## Structure
The idea behind this library is offer a fluent syntax to edit and read xml.

### Transformations
 The syntax to edit xml is very intuitive, first of all you need to define a Modification Rule 
 and then pass it as argument to `transform` method provided via extensions methods in `NodeSeq` class. 
 `transform` method will return the XML edited.
 
 A modification rule is composed by a `Zoom` function and `XmlModifier`, the first one has the aim to zoom inside document
 and select the node to edit, the second one apply a transformation over selected node.
 
 The pseudo code for this is something like: `modifier.apply(zoom.apply(document))`
 
 Node integrated with `AdvXml` there are implicits in order to add a more fluent syntax for rule creation.
 Each example is offered in two version raw and "with sugar", imports are the same.
 
 <i>Actors</i>
 - **XmlModifier** = Object that represent an XML modification, is a function like `NodeSeq => F[NodeSeq]` 
    - *ComposableXmlModifier* = Modifier that can be combined with other `ComposableXmlModifier`
    - *FinalXmlModifier* = Modifier that can not be combine with other `XmlModifier`, for example `Remove`
 - **Zoom** = Type alias to `NodeSeq => NodeSeq` used to zoom on specific node
 - **PartialXmlRule** = An incomplete `XmlRule` so it has the `Zoom` instance but no the `XmlModifier`
 - **XmlRule** = An object that contains `Zoom` instance and `XmlModifier`, this class provides a method to create the 
 scala xml `RewriteRule`
 
 <i>Syntax in a nutshell</i>
 - $ create a partial rule
 - ==> is an alias to `withModifier` method
 
 *Raw Example*
```scala
    import com.github.geirolz.advxml.AdvXml._
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
    
    val rule: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
        .withModifier(Append(<Car Brand="Lamborghini"/>))
        
    val result: Try[NodeSeq] = doc.transform[Try](rule)  
```

 *Example with sugar*
```scala
    val rule: XmlRule = $(_ \ "Person" \ "Cars") ==> Append(<Car Brand="Lamborghini"/>)
```

#### Multiple modifiers
If you need apply more that one modification on a selected node you can combine actions calling again `withModifier` method.

 *Raw Example*
```scala
    import com.github.geirolz.advxml.AdvXml._
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
    
    val rules: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
        .withModifier(Append(<Car Brand="Lamborghini"/>))
        .withModifier(Append(<Car Brand="Ferrari"/>))
        .withModifier(Append(<Car Brand="Bmw"/>))
        
    val result: Try[NodeSeq] = doc.transform[Try](rules)  
```

 *Example with sugar*
```scala
    val rules = $(_ \ "Person" \ "Cars") 
      ==> Append(<Car Brand="Lamborghini"/>) 
      ==> Append(<Car Brand="Ferrari"/>) 
      ==> Append(<Car Brand="Bmw"/>)
```

#### Combine modifiers
You can combine multiple modifiers using `andThen` method or with syntax sugar `++`

 *Raw Example*
```scala
    import com.github.geirolz.advxml.AdvXml._
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
    
    val rules: XmlRule = PartialXmlRule(_ \ "Person" \ "Cars")
        .withModifier(Append(<Car Brand="Lamborghini"/>)
          .andThen(Append(<Car Brand="Ferrari"/>)
          .andThen(Append(<Car Brand="Bmw"/>))))
        
    val result: Try[NodeSeq] = doc.transform[Try](rules)  
```
 *Example with sugar*
```scala
    val rules = $(_ \ "Person" \ "Cars") 
      ==> Append(<Car Brand="Lamborghini"/>) 
          ++ Append(<Car Brand="Ferrari"/>) 
          ++ Append(<Car Brand="Bmw"/>)

```

#### Root transformation
If you need to edit the document root you can invoke `transform` method passing directly 
the `XmlModifier` and no the `XmlRule` instance, this means no zooming actions.

 *Example*
```scala
    import com.github.geirolz.advxml.AdvXml._
    import com.github.geirolz.advxml.transform._
    import scala.xml._
    import scala.util._
    
    //import MonadError instance for Try
    import cats.instances.try_._
    
    val doc: Elem = <Root/>
    val result: Try[NodeSeq] = doc.transform[Try](SetAttrs("Attr1" -> "TEST"))
```

