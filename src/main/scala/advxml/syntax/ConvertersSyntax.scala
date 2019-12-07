package advxml.syntax

import advxml.core.convert._
import advxml.core.validate.ValidatedEx
import cats.{Applicative, Id, Monad}

import scala.annotation.implicitNotFound
import scala.xml.{NodeSeq, Text}

private[syntax] trait ConvertersSyntax {

  implicit class ApplicativeConverterOps[F[_]: Applicative, A](t: F[A]) {
    def mapAs[B](implicit s: UnsafeConverter[A, B]): F[Id[B]] = Applicative[F].map(t)(Converter(_))
  }

  implicit class MonadConverterOps[F[_]: Monad, A](t: F[A]) {
    def flatMapAs[B](implicit s: Converter[F, A, B]): F[B] = Monad[F].flatMap(t)(Converter(_))
  }

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def deepMap[B](f: A => B): F[G[B]] = Applicative[F].map(fg)(Applicative[G].map(_)(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def deepFlatMap[B](f: A => G[B]): F[G[B]] = Applicative[F].map(fg)(Monad[G].flatMap(_)(f))
  }

  //TODO: Maybe i can split this class into multiple implicit classes
  implicit class ValidatedConverterAnyOps[A](a: A) {

    /**
      * Convert [[A]] into [[B]] using implicit [[Converter]] if available
      * and if it conforms to required types [[F]], [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    @implicitNotFound("Missing Converter to transform object into ${F} of ${B}.")
    def as[F[_], B](implicit F: Converter[F, A, B]): F[B] = Converter.apply(a)

    /**
      * Convert [[A]] into [[B]] using implicit [[ValidatedConverter]] if available
      * and if it conforms to required types [[A]] and [[B]].
      *
      * @see [[Converter]] for further information.
      */
    @implicitNotFound("Missing ValidatedConverter to transform object into ValidatedConverter[${B}].")
    def as[B](implicit F: ValidatedConverter[A, B]): ValidatedEx[B] = Converter.apply(a)

    /**
      * Convert [[A]] into [[X]] using implicit [[ModelToXml]] if available
      * and if it conforms to required types [[A]] and [[X]]
      *
      * @see [[XmlConverter.asXml()]] for further information.
      * @see [[ValidatedConverter]] for further information.
      */
    @implicitNotFound("Missing ModelToXml to transform object into ValidatedEx[${X}].")
    def asXml[X <: NodeSeq](implicit F: ModelToXml[A, X]): ValidatedEx[X] = XmlConverter.asXml(a)

    /**
      * Convert [[A]] to a [[Text]] using implicit [[TextConverter]] if available
      * and if it conforms to required types [[A]]
      *
      * @return [[Text]] representation of [[A]]
      */
    @implicitNotFound("Missing TextConverter to transform object into Text.")
    def asText(implicit s: TextConverter[A]): Text = TextConverter(a)
  }
}
