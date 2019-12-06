package advxml.instances

import advxml.core.converters.{Converter, TextConverter, UnsafeConverter}
import cats.{Applicative, Id}
import cats.data.Kleisli

import scala.math.ScalaNumber
import scala.xml.Text

private[instances] trait ConvertersInstances extends CommonConvertersInstances with TextConverterInstances

private[instances] sealed trait CommonConvertersInstances {
  implicit def safeIdentityConverter[F[_]: Applicative, A]: Converter[F, A, A] = Converter.id[F, A]
  implicit def unsafeIdentityConverter[A]: UnsafeConverter[A, A] = Converter.id[Id, A]
}

/**
  * This trait provides standard and basic implementations of common [[TextConverter]].
  */
private[instances] sealed trait TextConverterInstances {
  // format: off
  implicit val text_converter_text         : TextConverter[Text]         = Converter.id
  implicit val text_converter_string       : TextConverter[String]       = toText[String]
  implicit val text_converter_scalaNumber  : TextConverter[ScalaNumber]  = toText[ScalaNumber]
  implicit val text_converter_byte         : TextConverter[Byte]         = toText[Byte]
  implicit val text_converter_short        : TextConverter[Short]        = toText[Short]
  implicit val text_converter_char         : TextConverter[Char]         = toText[Char]
  implicit val text_converter_int          : TextConverter[Int]          = toText[Int]
  implicit val text_converter_long         : TextConverter[Long]         = toText[Long]
  implicit val text_converter_float        : TextConverter[Float]        = toText[Float]
  implicit val text_converter_double       : TextConverter[Double]       = toText[Double]
  
  private def toText[I] : TextConverter[I] = Kleisli[Id, I, Text](v => Text(v.toString))
  // format: on
}
