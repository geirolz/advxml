# Normalize
Normalization feature allows to collapse empty nodes and trim texts.

We can use directly `XmlNormalizer` or importing `advxml.syntax.*` you can have normalization 
methods onto `NodeSeq` instance.
 
#### Example
```scala   
import advxml.transform.XmlNormalizer
import scala.xml.{Elem, NodeSeq}

val elem: Elem = <bar><foo></foo></bar>
val result: NodeSeq = XmlNormalizer.normalize(elem)
//result will be 
//<bar><foo/></bar>
```

#### Example with syntax
```scala
import scala.xml.{Elem, NodeSeq}
import advxml.syntax.*

val elem: Elem = <bar><foo></foo></bar>
val result:NodeSeq = elem.normalize
//result will be 
//<bar><foo/></bar>
```


