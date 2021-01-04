package advxml.testUtils

import advxml.core.transform.XmlNormalizer
import org.scalactic.{CanEqual, Equality}

import scala.util.Try
import scala.xml.NodeSeq

object ScalacticXmlEquality {

  implicit def streamlinedXmlNormalizedEquality[T <: NodeSeq]: Equality[T] =
    (a: T, b: Any) =>
      Try(
        XmlNormalizer.normalizedEquals(a, b.asInstanceOf[NodeSeq])
      ).getOrElse(false)

  implicit def streamlinedXmlNormalizedCanEqual[A <: NodeSeq, B <: NodeSeq]: A CanEqual B =
    (a: A, b: B) => streamlinedXmlNormalizedEquality[A].areEqual(a, b)
}
