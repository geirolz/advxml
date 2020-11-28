package advxml.syntax

import advxml.core.{data, XmlNormalizer}
import advxml.core.data._
import cats.{Applicative, Eq, Monad, PartialOrder}

import scala.xml.{NodeSeq, Text}

private[advxml] trait AllSyntax
    extends AllCommonSyntax
    with AllTransformSyntax
    with ConvertersSyntax
    with ValidatedSyntax
    with NormalizerSyntax
    with JavaScalaConvertersSyntax

private[advxml] trait AllCommonSyntax
    extends NormalizerSyntax
    with AttributeSyntax
    with PredicateSyntax
    with NestedMapSyntax

//************************************ NORMALIZE ****************************************
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

//******************************* ATTRIBUTE PREDICATE ***********************************
private[syntax] trait AttributeSyntax {

  implicit class KeyStringInterpolationOps(ctx: StringContext) {
    def k(args: Any*): Key = Key(ctx.s(args: _*))
  }

  implicit class AttributeOps(key: Key) {

    def :=[T](v: T)(implicit converter: PureConverter[T, Text]): AttributeData =
      data.AttributeData(key, converter(v))

    //********* KeyValuePredicate *********
    import cats.syntax.order._

    def ->[T](valuePredicate: T => Boolean): KeyValuePredicate[T] =
      KeyValuePredicate(key, valuePredicate)

    def ===[T: Eq](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ === that)

    def =!=[T: Eq](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ =!= that)

    def <[T: PartialOrder](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ < that)

    def <=[T: PartialOrder](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ <= that)

    def >[T: PartialOrder](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ > that)

    def >=[T: PartialOrder](that: T)(implicit converter: PureConverter[String, T]): KeyValuePredicate[String] =
      buildPredicate[T](_ >= that)

    private def buildPredicate[T](valuePredicate: T => Boolean)(implicit
      converter: PureConverter[String, T]
    ): KeyValuePredicate[String] =
      KeyValuePredicate(key, v => valuePredicate(converter(v)))
  }
}

//************************************ PREDICATE ****************************************
private[syntax] trait PredicateSyntax {
  implicit class PredicateOps[T](p: T => Boolean) {

    /** Combine with another predicate(`T => Boolean`) with `And` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `And` operator.
      */
    def &&(that: T => Boolean): T => Boolean = p.and(that)

    /** Combine with another predicate(`T => Boolean`) with `And` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `And` operator.
      */
    def and(that: T => Boolean): T => Boolean = Predicate.and(p, that)

    /** Combine with another predicate(`T => Boolean`) with `Or` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `Or` operator.
      */
    def ||(that: T => Boolean): T => Boolean = p.or(that)

    /** Combine with another predicate(`T => Boolean`) with `Or` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `Or` operator.
      */
    def or(that: T => Boolean): T => Boolean = Predicate.or(p, that)
  }
}

//************************************ NESTED MAP ***************************************
private[syntax] trait NestedMapSyntax {

  import cats.implicits._

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}
