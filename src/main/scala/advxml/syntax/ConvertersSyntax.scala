package advxml.syntax

import advxml.core.data.{Converter, PureConverter, ValidatedConverter, ValidatedNelEx}
import cats.{Applicative, Id, Monad}
import cats.implicits._

private[syntax] trait ConvertersSyntax {

  implicit class ApplicativeConverterOps[F[_]: Applicative, A](fa: F[A]) {
    def mapAs[G[_], B](implicit s: Converter[G, A, B]): F[G[B]] = fa.map(Converter[G, A, B].run(_))
    def mapAs[B](implicit s: PureConverter[A, B], i: DummyImplicit): F[B] = fa.mapAs[Id, B]
  }

  implicit class MonadConverterOps[F[_]: Monad, A](fa: F[A]) {
    def flatMapAs[B](implicit s: Converter[F, A, B]): F[B] = fa.flatMap(Converter[F, A, B].run(_))
  }

  implicit class AnyConverterOps[A](a: A) {

    /** Convert [[A]] into [[B]] using implicit [[Converter]] if available
      * and if it conforms to required types [[F]], [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    def as[F[_], B](implicit F: Converter[F, A, B]): F[B] =
      Converter[F, A, B].run(a)

    /** Convert [[A]] into [[B]] using implicit [[PureConverter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[PureConverter]] for further information.
      */
    def as[B](implicit F: PureConverter[A, B], i1: DummyImplicit): B =
      PureConverter[A, B].run(a)

    /** Convert [[A]] into [[B]] using implicit [[ValidatedConverter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    def asValidated[B](implicit F: ValidatedConverter[A, B]): ValidatedNelEx[B] =
      ValidatedConverter[A, B].run(a)
  }
}
