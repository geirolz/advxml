# Normalize
Normalization feature allows to collapse empty nodes and trim texts.

We can use directly `XmlNormalizer` or importing `advxml.syntax.*` you can have normalization 
methods onto `NodeSeq` instance.
 
#### Example
```scala mdoc:to-string
import advxml.transform.XmlNormalizer
import scala.xml.{Elem, NodeSeq}

val elem: Elem = <bar><foo></foo></bar>
val result: NodeSeq = XmlNormalizer.normalize(elem)
```

#### Example with syntax
```scala mdoc:reset:to-string
import scala.xml.{Elem, NodeSeq}
import advxml.implicits.*

val elem: Elem = <bar><foo></foo></bar>
val result:NodeSeq = elem.normalize
```


