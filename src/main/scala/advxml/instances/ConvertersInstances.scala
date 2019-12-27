package advxml.instances

import advxml.core.convert.{Converter, PureConverter, TextConverter}
import cats.Applicative

import scala.math.ScalaNumber
import scala.xml.Text

private[instances] trait ConvertersInstances extends CommonConvertersInstances with TextConverterInstances

private[instances] sealed trait CommonConvertersInstances {
  implicit def identityConverter[F[_]: Applicative, A]: Converter[F, A, A] = Converter.id[F, A]
  implicit def pureIdentityConverter[A]: PureConverter[A, A] = PureConverter.id[A]
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
  
  private def toText[I] : TextConverter[I] = PureConverter.of(v => Text(v.toString))
  // format: on
}
