package advxml

import advxml.core.validate.{EitherEx, ValidatedNelEx}
import advxml.core.Predicate
import advxml.core.convert.PureConverter
import advxml.core.transform.actions.{AttributeData, Key, KeyValuePredicate}
import cats.{Applicative, Monad}

import scala.util.Try
import scala.xml.Text

/*
 * In order to keep project clean keep in mind the following rules:
 * - Each feature can provide a trait that contains ONLY the syntax, NO public object or explicit public class.
 * - Each object represent a feature
 * - Each feature must provide a trait containing all syntax implicits named `[feature_name]Syntax`
 * - For each object must be exist a package with the same name under `advxml`
 */
/** This object is the entry point to access to all syntax implicits provided by Advxml.
  *
  * You can import all implicits using:
  * {{{
  *   import advxml.implicits._
  * }}}
  *
  * Otherwise you can import only a specific part of implicits using:
  * {{{
  *   //import advxml.implicits.[feature_name]._
  *   //example
  *   import advxml.implicits.transform._
  * }}}
  */
package object syntax {

  import cats.implicits._

  // format: off
  object all              extends AllSyntax
  object transform        extends XmlTransformerSyntax
  object convert          extends ConvertersSyntax
  object normalize        extends XmlNormalizerSyntax
  object validate         extends ValidationSyntax
  object javaConverters   extends JavaScalaConvertersSyntax
  object traverse         extends XmlTraverserSyntax {
    object try_             extends XmlTraverserSyntaxSpecified[Try, Option]
    object option           extends XmlTraverserSyntaxSpecified[Option, Option]
    object either           extends XmlTraverserSyntaxSpecified[EitherEx, Option]
    object validated        extends XmlTraverserSyntaxSpecified[ValidatedNelEx, Option]
  }
  // format: on

  //************************************* ATTRIBUTE PREDICATE ****************************************
  implicit class KeyStringInterpolationOps(ctx: StringContext) {
    def k(args: Any*): Key = Key(ctx.s(args: _*))
  }

  implicit class AttributeOps(key: Key) {

    def :=[T](v: T)(implicit converter: PureConverter[T, Text]): AttributeData =
      AttributeData(key, converter(v))

    def ===[T](that: T)(implicit converter: PureConverter[T, String]): KeyValuePredicate[String] =
      KeyValuePredicate(key, _ == converter(that))

    def ->[T](valuePredicate: T => Boolean): KeyValuePredicate[T] =
      KeyValuePredicate(key, valuePredicate)

    //********* FOR NUMBERS *********
    def <(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ < _)

    def <=(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ <= _)

    def >(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ > _)

    def >=(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ >= _)

    private def buildPredicateForScalaNumber(that: Double, p: (Double, Double) => Boolean)(implicit
      converter: PureConverter[String, Double]
    ): KeyValuePredicate[String] =
      key -> (v => p(converter(v), that))
  }

  //************************************* NESTED MAP ****************************************
  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }

  //************************************* PREDICATE ****************************************
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
