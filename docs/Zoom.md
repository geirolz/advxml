# XmlZoom
This feature allow users read/obtain node(s) or attributes.

You can see zoom as a function that traverse the xml and select one or more elements.
This feature core is written with tagless final and all methods
returns an output value wrapped in `F[_]`.

- [How to build](#how-to-build)
- [How to run](#how-to-run)
- [XmlContentZoom](#attributes-and-text)
---
### How to build

Zoom has two types, we can have `XmlZoom`(unbinded) or `BindedXmlZoom`.
The differences between _Unbinded_ and _Binded_ is that Unbinded doesn't know the target
but contains only the list of action to do, while _Binded_ contains both actions and target.

It is possible to convert an `XmlZoom`(Unbinded) to `BindedXmlZoom` and vice-versa using
`bind` and `unbind` idempotent methods.

```scala
import advxml.core.transform.{XmlZoom, BindedXmlZoom}

val zoom : XmlZoom = ??? //unbinded
val binded : BindedXmlZoom = zoom.bind(<foo />) //binded
val unbinded : XmlZoom = zoom.unbind() //unbinded again
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


---
### How to run
To run a zoom you need to have a "target", a `NodeSeq` instance.
So if you have an unbinded `XmlZoom` conceptually you have to pass a `NodeSeq` instance as target
when you invoke the zoom, while `BindedXmlZoom` already has this information so is not necessary specify the zoom target.

To run a zoom we can use two method, `run` and `detailed`.
- `run` return a `F[NodeSeq]` as result of the zooming action.
- `detailed` return a `F[XmlZoomResult]` as result of the zooming action that contains also the path information step by step.

```scala
import advxml.core.transform.{XmlZoom, BindedXmlZoom, XmlZoomResult}
import scala.util.Try
import scala.xml.NodeSeq
import advxml.implicits._

//XmlZoom
val unbinded : XmlZoom = ???
val unbindedRun : Try[NodeSeq] = unbinded.run[Try](<foo/>)
val unbindedDetailed : Try[XmlZoomResult] = unbinded.detailed[Try](<foo/>)

//BindedXmlZoom
val binded : BindedXmlZoom = unbinded.bind(<foo/>)
val bindedRun : Try[NodeSeq] = binded.run[Try]
val bindedsDetailed : Try[XmlZoomResult] = binded.detailed[Try]
```

---
### Attributes and Text
Advxml provides also an `XmlContetZoom` to read attributes as text from an NodeSeq instance.
`XmlContetZoom` syntax is added to `NodeSeq`, `F[NodeSeq]` and `XmlZoom` using implicit class.

#### Example
```scala
import advxml.core.transform.XmlZoom.{$, root}
import advxml.implicits._
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

//Immediate Child with XmlZoom - Unbinded
val mandatoryNode: Try[NodeSeq] = $(doc).Person.Cars.run[Try]
val optionalNode: Option[NodeSeq] = $(doc).Person.Cars.run[Option]

//Immediate Child with XmlZoom - Unbinded
val mandatoryNodeB: Try[NodeSeq] = root.Person.Cars.run[Try](doc)
val optionalNodeB: Option[NodeSeq] = root.Person.Cars.run[Option](doc)

//Attributes
val mandatoryAttr: Try[String] = $(doc).Person.Cars./@[Try]("Brand")
val optionalAttr: Option[String] = $(doc).Person.Cars./@[Option]("Brand")

//Text
val mandatoryText: Try[String] = $(doc).Person.Cars.textM[Try]
val optionalText: Option[String] = $(doc).Person.Cars.textM[Option]
```
