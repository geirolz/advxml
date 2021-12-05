# XmlZoom
This feature allows users read/obtain nodes or attributes.

We can see zoom as a function that traverse the xml and select one or more elements.
This feature core is written with tagless final and all methods
returns an output value wrapped in `F[_]`.

- [How to build](#how-to-build)
- [How to run](#how-to-run)
- [Attributes and text](#attributes-and-text)

Given
```scala
import advxml.implicits.*
import advxml.transform.*
import advxml.transform.XmlZoom.{$, root}
```

---
### How to build

Zoom has two types, we can have `XmlZoom`(unbinded) or `BindedXmlZoom`.
The differences between _Unbinded_ and _Binded_ is that _Unbinded_ doesn't know the target
and contains only the list of action to do, while _Binded_ contains both actions and target.

It is possible to convert an `XmlZoom`(Unbinded) to `BindedXmlZoom` and vice-versa using
`bind` and `unbind` idempotent methods.

```scala
val zoom: XmlZoom = XmlZoom.root //unbinded
// zoom: XmlZoom = Unbinded(List()) //unbinded
val binded: BindedXmlZoom = zoom.bind(<foo/>) //binded
// binded: BindedXmlZoom = Binded(<foo/>,List()) //binded
val unbinded: XmlZoom = zoom.unbind() //unbinded again
// unbinded: XmlZoom = Unbinded(List())
```

###### XmlZoom(unbinded)
- `apply(actions: List[ZoomAction])` = Create an unbinded instance with specified actions.
- `empty` = Create an unbinded instance with empty actions
- `root` = Alias to `empty`, no zooming actions means the pointer is on the root.
- `$` = Alias to `empty`, useful when to define a new zoom that doesn't start from the root.

###### BindedXmlZoom
- `root(NodeSeq)` = The binded version on unbinded `root`
- `$(NodeSeq)` = The binded version on unbinded `$`

Once created we can append actions using the following methods:
- `immediateChild(String)` = Find the first node with specified name, syntax alias is `/`(slash).
- `filter(XmlPredicate)` = Filters nodes that match the specified predicate, syntax alias is `|`.
- `find(XmlPredicate)` = Find the first node that match the specified predicate.
- `head` = Get the first node.
- `last` = Get the last node.
- `atIndex(Int)` = Get the node at specified index.

`XmlZoom` and `BindedXmlZoom` both extends `Dynamic` so you can use dot notation instead of `immediateChild`
thanks to `selectDynamic`. We can even combine `immediateChild` and `atIndex` using `applyDynamic`

```scala
//this equals to root.down("foo").down("bar").down("text")
//this equals to root / "foo" / "bar" / "text"
val selectDynamicZoom: XmlZoom = root.foo.bar.test
// selectDynamicZoom: XmlZoom = Unbinded(
//   actions = List(Down(value = "foo"), Down(value = "bar"), Down(value = "test"))
// )

//this equals to root.down("foo").down("bar").down("text").atIndex(1)
//this equals to root / "foo" / "bar" / "text" atIndex(1)
val applyDynamicZoom: XmlZoom = root.foo.bar.test(1)
// applyDynamicZoom: XmlZoom = Unbinded(
//   actions = List(
//     Down(value = "foo"),
//     Down(value = "bar"),
//     Down(value = "test"),
//     AtIndex(idx = 1)
//   )
// )
```

---
### How to run
To run a zoom we need to have a "target", a `NodeSeq` instance.
So if we have an unbinded `XmlZoom` conceptually we have to pass a `NodeSeq` instance as target when we invoke the zoom, 
while `BindedXmlZoom` already has this information so is not necessary specify the zoom target when you need to run it.

To run a zoom we can use two method, `run` and `detailed`.
- `run` return a `F[NodeSeq]` as result of the zooming action.
- `detailed` return a `F[XmlZoomResult]` as result of the zooming action that contains also the path information step by step.

```scala
import scala.util.Try
import scala.xml.NodeSeq

//XmlZoom
val unbinded: XmlZoom = XmlZoom.root
// unbinded: XmlZoom = Unbinded(List())
val unbindedRun: Try[NodeSeq] = unbinded.run[Try](<foo/>)
// unbindedRun: Try[NodeSeq] = Success(<foo/>)
val unbindedDetailed: Try[XmlZoomResult] = unbinded.detailed[Try](<foo/>)
// unbindedDetailed: Try[XmlZoomResult] = Success(Result(<foo/>,List()))

//BindedXmlZoom
val binded: BindedXmlZoom = unbinded.bind(<foo/>)
// binded: BindedXmlZoom = Binded(<foo/>,List())
val bindedRun: Try[NodeSeq] = binded.run[Try]
// bindedRun: Try[NodeSeq] = Success(<foo/>)
val bindedsDetailed: Try[XmlZoomResult] = binded.detailed[Try]
// bindedsDetailed: Try[XmlZoomResult] = Success(Result(<foo/>,List()))
```

---
### Attributes and Text
Advxml also provides an `XmlContetZoom` to read attributes and text from a NodeSeq instance.
`XmlContetZoom` syntax is added to `NodeSeq`, `F[NodeSeq]`, `XmlZoom` and `BindedXmlZoom` using implicit class.

We can use `attr` to get an attribute value or `content` to get the node content.

```scala
import advxml.transform.XmlZoom.root
import cats.instances.try_.*

import scala.util.Try
import scala.xml.Elem

val doc: Elem = <foo T1='1'>TEXT</foo>
// doc: Elem = <foo T1="1">TEXT</foo>
val zoom: XmlZoom = XmlZoom.root
// zoom: XmlZoom = Unbinded(List())
val bindedZoom: BindedXmlZoom = XmlZoom.root(<foo T1='1'>TEXT</foo>)
// bindedZoom: BindedXmlZoom = Binded(<foo T1="1">TEXT</foo>,List())

val docAttr: Try[String] = doc.attr("T1").extract[Try] //Success("1")
// docAttr: Try[String] = Success(1) //Success("1")
val docMissingAttr: Try[String] = doc.attr("MISSING").extract[Try] //Failure(ValidationRule.Error)
// docMissingAttr: Try[String] = Failure(java.lang.RuntimeException: 
// # Value: foo /@ MISSING => ""
// - FAILED RULES(1)
// [ ] NonEmpty: Empty value.
// -------------------------------------
// 
// ) //Failure(ValidationRule.Error)
val docText: Try[String] = doc.content.extract[Try] //Success("TEXT")
// docText: Try[String] = Success(TEXT) //Success("TEXT")

val zoomAttr: Try[String] = zoom.attr(<foo T1='1'>TEXT</foo>, "T1").extract[Try] //Success("1")
// zoomAttr: Try[String] = Success(1) //Success("1")
val zoomText: Try[String] = zoom.content(<foo T1='1'>TEXT</foo>).extract[Try] //Success("TEXT")
// zoomText: Try[String] = Success(TEXT) //Success("TEXT")

val bindedZoomAttr: Try[String] = bindedZoom.attr("T1").extract[Try] //Success("1")
// bindedZoomAttr: Try[String] = Success(1) //Success("1")
val bindedZoomText: Try[String] = bindedZoom.content.extract[Try] //Success("TEXT")
// bindedZoomText: Try[String] = Success(TEXT)
```

This feature combined to converter feature allows we to convert attributes data from `String` to another type.

```scala
import advxml.transform.XmlZoom.root
import cats.instances.try_.*

import scala.util.Try
import scala.xml.Elem

val doc: Elem = <foo T1='1'>100</foo>
// doc: Elem = <foo T1="1">100</foo>
val docAttr: Try[Int] = doc.attr("T1").as[Try[Int]] //Success(1)
// docAttr: Try[Int] = Success(1) //Success(1)
val docText: Try[Int] = doc.content.as[Try[Int]] //Success(100)
// docText: Try[Int] = Success(100)
```


#### Full Example

```scala
import advxml.transform.XmlZoom.{$, root}
import advxml.implicits.*
import scala.xml.*
import scala.util.Try
import cats.instances.try_.*
import cats.instances.option.*

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
// doc: Elem = <Persons>
//     <Person Name="Mimmo">
//       <Cars>
//         <Car Brand="Fiat">
//           <Price>10000€</Price>
//         </Car>
//       </Cars>
//     </Person>
//   </Persons>

//Immediate Child with XmlZoom - Binded
val mandatoryNode: Try[NodeSeq] = $(doc).Person.Cars.run[Try]
// mandatoryNode: Try[NodeSeq] = Success(<Cars>
//         <Car Brand="Fiat">
//           <Price>10000€</Price>
//         </Car>
//       </Cars>)
val optionalNode: Option[NodeSeq] = $(doc).Person.Cars.run[Option]
// optionalNode: Option[NodeSeq] = Some(<Cars>
//         <Car Brand="Fiat">
//           <Price>10000€</Price>
//         </Car>
//       </Cars>)

//Immediate Child with XmlZoom - Unbinded
val mandatoryNodeB: Try[NodeSeq] = root.Person.Cars.run[Try](doc)
// mandatoryNodeB: Try[NodeSeq] = Success(<Cars>
//         <Car Brand="Fiat">
//           <Price>10000€</Price>
//         </Car>
//       </Cars>)
val optionalNodeB: Option[NodeSeq] = root.Person.Cars.run[Option](doc)
// optionalNodeB: Option[NodeSeq] = Some(<Cars>
//         <Car Brand="Fiat">
//           <Price>10000€</Price>
//         </Car>
//       </Cars>)

//Attributes
val mandatoryAttr: Try[String] = $(doc).Person.Cars.attr("Brand").as[Try[String]]
// mandatoryAttr: Try[String] = Failure(java.lang.RuntimeException: 
// # Value:  /@ Brand => ""
// - FAILED RULES(1)
// [ ] NonEmpty: Empty value.
// -------------------------------------
// 
// )
val optionalAttr: Option[String] = $(doc).Person.Cars.attr("Brand").as[Option[String]]
// optionalAttr: Option[String] = None

//Content
val mandatoryText: Try[String] = $(doc).Person.Cars.content.as[Try[String]]
// mandatoryText: Try[String] = Success(
//         
//           10000€
//         
//       )
val optionalText: Option[String] = $(doc).Person.Cars.content.as[Option[String]]
// optionalText: Option[String] = Some(
//         
//           10000€
//         
//       )
```
