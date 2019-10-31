package com.github.geirolz.advxml.convert.impls

import cats.Applicative
import com.github.geirolz.advxml.convert.impls.Converter.UnsafeConverter
import com.github.geirolz.advxml.convert.impls.TextConverter.TextConverter

import scala.math.ScalaNumber
import scala.xml.Text

/**
  * Advxml
  * Created by geirolad on 03/07/2019.
  *
  * @author geirolad
  */
object TextConverter {

  type TextConverter[-A] = UnsafeConverter[A, Text]

  def mapAsText[F[_]: Applicative, A](fa: F[A])(implicit s: TextConverter[A]): F[Text] =
    Applicative[F].map(fa)(asText(_))

  def asText[A](a: A)(implicit f: TextConverter[A]): Text = f(a)
}

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
