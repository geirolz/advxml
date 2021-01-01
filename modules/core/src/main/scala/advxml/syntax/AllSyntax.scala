package advxml.syntax

import advxml.core.XmlNormalizer
import cats.{Applicative, Monad}

import scala.xml.NodeSeq

private[advxml] trait AllSyntax
    extends AllCommonSyntax
    with AllTransformSyntax
    with AllDataSyntax
    with NormalizerSyntax
    with JavaScalaConvertersSyntax

private[advxml] trait AllCommonSyntax extends NormalizerSyntax with NestedMapSyntax

//============================== NORMALIZE ==============================
private[syntax] trait NormalizerSyntax {

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

//============================== NESTED MAP ==============================
private[syntax] trait NestedMapSyntax {

  import cats.implicits._

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}
