package advxml.syntax

import advxml.core.data._
import advxml.core.AppExOrEu
import cats.{~>, Applicative, FlatMap}
import cats.data.Validated
import cats.implicits._

import scala.xml.NodeSeq

private[syntax] trait ConvertersSyntax {

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
    def to[G[_]: AppExOrEu](ifNone: => Throwable): G[A] =
      AppExOrEu.fromOption(ifNone)(fa)
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
    def mapAs[B](implicit c: Converter[A, B]): F[B] = fa.map(c.run(_))
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
    def flatMapAs[B](implicit c: Converter[A, F[B]]): F[B] = fa.flatMap(c.run(_))
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
