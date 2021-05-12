# Transform
The syntax to edit xml is very intuitive, first of all we need to define a **Modification Rule**
and then pass it as argument to `transform` method provided via extensions methods in `NodeSeq` class.
`transform` method will return the XML edited wrapped in `F[_]`.

A modification rule is composed by:
- `XmlZoom`: A case class with the aim to zoom inside document and select the node to edit.
- `XmlModifier`: A function that apply a transformation over selected node.

Note: Integrated with `AdvXml` there are implicits in order to add a more fluent syntax for rule creation.
Each example is written with fluent syntax using implicits but commented you can see the "Desugared" version.

#### Classes
- **XmlModifier** = Object that represent an XML modification, is a function like `NodeSeq => F[NodeSeq]`.
    - *ComposableXmlModifier* = Modifier that can be combined with other `ComposableXmlModifier`.
    - *FinalXmlModifier* = Modifier that can not be combine with other `XmlModifier`, for example `Remove`.
- **XmlZoom** = Case class that contains the traverse information to arrive to the target node.
- **XmlRule** = An object that contains `Zoom` instance and `XmlModifier`, this class provides a method to create the
  scala xml `RewriteRule`. This class inherit from `AbstractRule`(_we see this below in the *"combine rules"* section_).

#### Syntax
- **root** is the default XmlZoom that is empty so select the document root node, delegated to `XmlZoom.empty`
- **$** equals to `root` and `XmlZoom.empty` but with different name, you should use it when your zoom will not start from
  the root so using `root` variable can create confusion. This is very useful when you have a huge `XmlZoom` expression and
  you what to split in into smaller `XmlZoom`. Advxml provides the monoid implementation of `XmlZoom` to use `<+>` to combine them.
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

val rule: XmlRule = root.Person.Cars ==> Append(<Car Brand="Lamborghini"/>)

//  Desugared
//  val rule: XmlRule = root
//    .down("Person")
//    .down("Cars")
//    .withModifier(Append(<Car Brand="Lamborghini"/>))

val result: Try[NodeSeq] = doc.transform[Try](rule)
```

## Combine rules
We can combine rules using an abstraction hover the `XmlRule` class, the `AbstractRule`(this name can change in the future).
`AbstractRule` allow us to combine rules with `And` and `OrElse` operators, moreover using a similar logic as `OrElse` 
we can describe a rule as `Optional`, doing this if the rule fails it returns the xml document passed as input.

Given _R1_ and _R2_ where are both two simple rule when combine them we have the following behavior:

- _R1_ `and` _R2_ = If _R1_ or _R2_ fails the combined rule fails.
- _R1_ `orElse` _R2_ = If _R1_ fails we apply _R2_, if both fail the combined rule fails.
- _R1_ `optional` = Even if _R1_ fails the combined rule returns a success, but in case _R1_ fails 
  we have the input document as output(so without any changes).

#### Example

```scala
import advxml.core.transform.{AbstractRule, ComposableXmlRule}
import advxml.implicits._

import scala.xml._
import scala.util._

//import MonadError instance for Try
import cats.instances.try_._

val doc: NodeSeq = <Root></Root>
val r1: ComposableXmlRule = root ==> Append(<Node1/>)
val r2: ComposableXmlRule = root ==> Append(<Node1/>)

//Will try to apply both R1 and R2
val r1AndR2: AbstractRule = r1.and(r2)

//Will try to apply R1, if it fails will apply R2, if R2 fails r1OrR2 fails
val r1OrR2: AbstractRule = r1.orElse(r2)

//Will try to apply R1, even if it fails r1Optional will success but in case R1 fails the out is the input document without changes
val r1Optional: AbstractRule = r1.optional
```

## Combine modifiers
If we need apply more than one modification on
a selected node you can combine actions calling again `withModifier` method, or using some sugared syntax `==>`.

Doing this we can specify another `XmlModifier` to combine. It can be a `ComposableXmlModifier` or a `FinalXmlModifier`.
As suggested by the name, if we pass a `FinalXmlModifier` there is no more the possibility to continue the chain.

Actually the only implementation of `FinalXmlModifier` is just the `Delete` action, in this way we prevent at compile time
the deletion of a node before other actions, if you use `Delete` you can not do anything else on that node.

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
val rule = root.Person.Cars
             ==> Append(<Car Brand="Lamborghini"/>)
             ==> Append(<Car Brand="Ferrari"/>)
             ==> Append(<Car Brand="Bmw"/>)

//  Desugared
//  val rules: XmlRule = root
//      .down("Person")
//      .down("Cars")
//      .withModifier(Append(<Car Brand="Lamborghini"/>))
//      .withModifier(Append(<Car Brand="Ferrari"/>))
//      .withModifier(Append(<Car Brand="Bmw"/>))

val result: Try[NodeSeq] = doc.transform[Try](rule)  
```

You can also use the `Monoid` implementation provided in the instances to combine multiple `ComposableXmlModifier`

```scala

import advxml.implicits._
import cats.kernel.Monoid

import scala.xml._
import scala.util._
import cats.syntax.monoid._

val m1: ComposableXmlModifier = Append(<Car Brand="Lamborghini"/>)
val m2: ComposableXmlModifier = Append(<Car Brand="Ferrari"/>)
val m3: ComposableXmlModifier = Append(<Car Brand="Tesla"/>)

val m4: ComposableXmlModifier = Monoid.combineAll(m1, m2, m3)
val m4Sugar: ComposableXmlModifier = m1 |+| m2 |+| m3
```

## Root transformation
If we need to edit the document root we can use `root` as zoom action.
`root` value is provided by `advxml.instances.transform._`

*Example*
```scala
import scala.xml._
import scala.util._

//import MonadError instance for Try
import cats.instances.try_._
import advxml.implicits._

val doc: Elem = <Root/>
val result: Try[NodeSeq] = doc.transform[Try](root ==> SetAttrs("Attr1" := "TEST"))
```