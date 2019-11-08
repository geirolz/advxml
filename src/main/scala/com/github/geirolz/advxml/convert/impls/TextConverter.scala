package com.github.geirolz.advxml.convert.impls

import cats.Applicative
import com.github.geirolz.advxml.convert.impls.Converter.UnsafeConverter
import com.github.geirolz.advxml.convert.impls.TextConverter.TextConverter

import scala.annotation.implicitNotFound
import scala.math.ScalaNumber
import scala.xml.Text

/**
  * Advxml
  * Created by geirolad on 03/07/2019.
  *
  * @author geirolad
  */
object TextConverter {

  /**
    * Represents a function `A => Text` to simplify method and class signatures.
    * This alias represent an unsafe converter to transform `A` into `Text`.
    *
    * The invocation of this function can fail and/or throw an runtime exception.
    *
    * @see [[com.github.geirolz.advxml.convert.impls.Converter.UnsafeConverter]] for further information.
    * @tparam A Contravariant input object type
    */
  type TextConverter[-A] = UnsafeConverter[A, Text]

  def mapAsText[F[_]: Applicative, A](fa: F[A])(implicit s: TextConverter[A]): F[Text] =
    Applicative[F].map(fa)(asText(_))

  /**
    * Syntactic sugar to convert a `A` instance into `Text` using an implicit [[TextConverter]] instance.
    * This method catch a [[TextConverter]] instance in the scope that conforms with type `A` and then invoke
    * in it the method `apply` passing `a`.
    *
    * See [[TextConverter]] for further information.
    *
    * @param a Converter input
    * @param f Implicit instance of [[TextConverter]]
    * @tparam A Input type
    * @return `A` converted into `Text`
    */
  def asText[A](a: A)(implicit f: TextConverter[A]): Text = f(a)

  /**
    * Apply conversion using implicit [[TextConverter]] instance.
    *
    * @see [[Converter]] for further information.
    * @param a Input instance
    * @param F implicit [[TextConverter]] instance
    * @tparam A Contravariant input type
    * @return Unsafe conversion of `A` into `Text`
    */
  @implicitNotFound("Missing TextConverter to transform ${A} into `Text`")
  def apply[A](a: A)(implicit F: TextConverter[A]): Text = F.apply(a)
}

/**
  * This trait provides standard and basic implementations of common [[TextConverter]].
  */
private[convert] trait TextConverterInstances {
  // format: off
  implicit val text_converter_string       : TextConverter[String]       = v => Text(v.toString)
  implicit val text_converter_scalaNumber  : TextConverter[ScalaNumber]  = v => Text(v.toString)
  implicit val text_converter_byte         : TextConverter[Byte]         = v => Text(v.toString)
  implicit val text_converter_short        : TextConverter[Short]        = v => Text(v.toString)
  implicit val text_converter_char         : TextConverter[Char]         = v => Text(v.toString)
  implicit val text_converter_int          : TextConverter[Int]          = v => Text(v.toString)
  implicit val text_converter_long         : TextConverter[Long]         = v => Text(v.toString)
  implicit val text_converter_float        : TextConverter[Float]        = v => Text(v.toString)
  implicit val text_converter_double       : TextConverter[Double]       = v => Text(v.toString)
  // format: on
}
