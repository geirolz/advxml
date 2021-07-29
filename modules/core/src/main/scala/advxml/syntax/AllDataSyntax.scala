package advxml.syntax

import advxml.core.data._
import advxml.core.ApplicativeThrowOrEu
import cats.{~>, Applicative, Eq, FlatMap, PartialOrder}
import cats.data.Validated
import cats.implicits._

import scala.util.Try
import scala.xml.NodeSeq

private[syntax] trait AllDataSyntax extends ConverterSyntax with AttributeSyntax with PredicateSyntax

//============================== CONVERTER ==============================
private[syntax] trait ConverterSyntax {

  implicit class AnyFunctionKConverterSyntaxOps[F[_], A](fa: F[A]) {

    /** Change context from F[_] to G[_] using natural transformation with an implicit FunctionK instance.
      * @param nt functionK instance
      * @tparam G new context
      * @return same value but in G[_] context
      */
    def to[G[_]](implicit nt: F ~> G): G[A] =
      nt(fa)
  }

  implicit class OptionFunctionKConverterSyntaxOps[A](fa: Option[A]) {

    /** Change context from F[_] to G[_].
      * @param ifNone error used in case of none.
      * @tparam G new context
      * @return same value but in G[_] context
      */
    def to[G[_]: ApplicativeThrowOrEu](ifNone: => Throwable): G[A] =
      ApplicativeThrowOrEu.fromOption(ifNone)(fa)
  }

  implicit class ApplicativeConverterSyntaxOps[F[_]: Applicative, A](fa: F[A]) {

    /** Map running implicit converter.
      * This method is just a syntactic sugar for:
      * {{{
      * implicit val converter : Converter[A, B] = ???
      * val fa : F[A] = ???
      *
      * val result : F[B] = fa.map(a => converter.run(a))
      * }}}
      *
      * @param c converter instance.
      * @tparam B result inner type
      * @return [[B]] instance in F[_]
      */
    def mapAs[B](implicit c: Converter[A, B]): F[B] = fa.map(c.run)
  }

  implicit class FlatMapConverterSyntaxOps[F[_]: FlatMap, A](fa: F[A]) {

    /** FlatMap running implicit converter.
      * This method is just a syntactic sugar for:
      * {{{
      * implicit val converter : Converter[A, F[B]] = ???
      * val fa : F[A] = ???
      *
      * val result : F[B] = fa.flatMap(a => converter.run(a))
      * }}}
      *
      * @param c converter instance.
      * @tparam B result inner type
      * @return [[B]] instance in F[_]
      */
    def flatMapAs[B](implicit c: Converter[A, F[B]]): F[B] = fa.flatMap(c.run)
  }

  implicit class ValidatedAndThenConverterSyntaxOps[E, A](fa: Validated[E, A]) {

    /** [[Validated]] andThen running implicit converter.
      * This method is just a syntactic sugar for:
      * {{{
      * implicit val converter : Converter[A, Validated[E, B]] = ???
      * val fa : Validated[E, A] = ???
      *
      * val result : Validated[E, B] = fa.andThen(a => converter.run(a))
      * }}}
      *
      * @param c converter instance.
      * @tparam B result inner type
      * @return [[B]] instance in Validated[E, _]
      */
    def andThenAs[B](implicit c: Converter[A, Validated[E, B]]): Validated[E, B] = fa.andThen(a => c.run(a))
  }

  implicit class AnyConverterSyntaxOps[A](a: A) {

    /** Convert [[A]] into [[B]] using implicit [[Converter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    def as[B](implicit c: Converter[A, B]): B =
      c.run(a)

    /** Convert [[A]] into [[B]] using implicit [[ValidatedConverter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    def asValidated[B](implicit c: ValidatedConverter[A, B]): ValidatedNelEx[B] =
      c.run(a)

    /** Convert [[A]] into [[B]] using implicit [[Converter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    def asOption[B](implicit c: OptionConverter[A, B]): Option[B] =
      c.run(a)

    /** Syntactic sugar to run an implicit [[XmlEncoder]] with [[A]] instance as input.
      */
    def encode(implicit c: XmlEncoder[A]): ValidatedNelEx[NodeSeq] =
      c.run(a)
  }

  implicit class NodeSeqConverterSyntaxOps(ns: NodeSeq) {

    /** Syntactic sugar to run an implicit [[XmlDecoder]] with NodeSeq instance as input.
      */
    def decode[B](implicit c: XmlDecoder[B]): ValidatedNelEx[B] = c.run(ns)
  }
}

//============================== ATTRIBUTE PREDICATE ==============================
private[syntax] trait AttributeSyntax {

  implicit class KeyAndValueStringInterpolationOps(ctx: StringContext) {
    def k(args: Any*): Key = Key(ctx.s(args: _*))
    def v(args: Any*): SimpleValue = SimpleValue(ctx.s(args: _*))
  }

  implicit class AttributeOps(key: Key) {

    def :=[T](v: T)(implicit c: T As SimpleValue): AttributeData =
      AttributeData(key, c.run(v))

    //********* KeyValuePredicate *********
    import cats.syntax.order._

    def ->(valuePredicate: SimpleValue => Boolean): KeyValuePredicate =
      KeyValuePredicate(key, valuePredicate)

    def ===[T: Eq](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ === _, that, "===")

    def =!=[T: Eq](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ =!= _, that, "=!=")

    def <[T: PartialOrder](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ < _, that, "<")

    def <=[T: PartialOrder](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ <= _, that, "<=")

    def >[T: PartialOrder](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ > _, that, ">")

    def >=[T: PartialOrder](that: T)(implicit converter: SimpleValue As Try[T]): KeyValuePredicate =
      buildPredicate[T](_ >= _, that, ">=")

    private def buildPredicate[T](p: (T, T) => Boolean, that: T, symbol: String)(implicit
      c: SimpleValue As Try[T]
    ): KeyValuePredicate =
      KeyValuePredicate(
        key,
        new (SimpleValue => Boolean) {
          override def apply(f: SimpleValue): Boolean = c.run(f).map(p(_, that)).getOrElse(false)
          override def toString(): String = s"$symbol [$that]"
        }
      )
  }
}

//============================== PREDICATE ==============================
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
