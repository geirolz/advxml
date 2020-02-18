# Normalize
Normalization feature allows to collapse empty nodes and trim texts.

You can use directly `XmlNormalizer` or importing `advxml.syntax.normalize._` you can have normalization 
methods onto `NodeSeq` instance.
 
#### Example
```scala   
    import scala.xml.{Elem, NodeSeq}
    import advxml.core.XmlNormalizer

    val elem: Elem = <bar><foo></foo></bar>
    val result: NodeSeq = XmlNormalizer.normalize(elem)
    //result will be 
    //<bar><foo/></bar>
```

#### Example with syntax
```scala
    import scala.xml.{Elem, NodeSeq}
    import advxml.syntax.normalize._

    val elem: Elem = <bar><foo></foo></bar>
    val result:NodeSeq = elem.normalize
    //result will be 
    //<bar><foo/></bar>
```


