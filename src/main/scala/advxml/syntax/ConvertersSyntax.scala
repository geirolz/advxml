package advxml.syntax

import advxml.core.converters._
import advxml.core.validation.ValidatedEx
import cats.Applicative

import scala.annotation.implicitNotFound
import scala.xml.{NodeSeq, Text}

private[syntax] trait ConvertersSyntax {

  implicit class TextConverterApplicativeOps[F[_]: Applicative, A](t: F[A]) {
    def mapAsText(implicit s: TextConverter[A]): F[Text] = TextConverter.mapAsText(t)
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
