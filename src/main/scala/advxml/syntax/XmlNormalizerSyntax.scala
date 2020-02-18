package advxml.syntax

import advxml.core.XmlNormalizer
import org.scalactic.Equality

import scala.util.Try
import scala.xml.NodeSeq

private[syntax] trait XmlNormalizerSyntax {

  implicit def streamlinedXmlNormalizedEquality[T <: NodeSeq]: Equality[T] =
    (a: T, b: Any) =>
      Try(
        XmlNormalizer.normalizedEquals(a, b.asInstanceOf[NodeSeq])
      ).getOrElse(false)

  implicit class NodeSeqNormalizationAndEqualityOps(ns: NodeSeq) {

    def normalize: NodeSeq =
      XmlNormalizer.normalize(ns)

    def normalizedEquals(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |==|(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |!=|(ns2: NodeSeq): Boolean =
      !XmlNormalizer.normalizedEquals(ns, ns2)
  }
}
