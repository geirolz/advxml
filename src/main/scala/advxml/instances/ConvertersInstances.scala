package advxml.instances

import advxml.core.convert.{Converter, PureConverter}
import advxml.core.validate.MonadEx
import cats.Applicative

import scala.math.ScalaNumber
import scala.util.{Success, Try}
import scala.xml.Text

private[instances] trait ConvertersInstances extends CommonConvertersInstances with TextConverterInstances

private[instances] sealed trait CommonConvertersInstances {
  implicit def identityConverter[F[_]: Applicative, A]: Converter[F, A, A] = Converter.id[F, A]
  implicit def pureIdentityConverter[A]: PureConverter[A, A] = PureConverter.id[A]
}

private[instances] sealed trait TextConverterInstances {

  // format: off
  implicit def text_converter_text       [F[_] : Applicative]  : Converter[F, Text, Text]         = Converter.id
  implicit def text_converter_string     [F[_] : Applicative]  : Converter[F, String, Text]       = toText
  implicit def text_converter_scalaNumber[F[_] : Applicative]  : Converter[F, ScalaNumber, Text]  = toText
  implicit def text_converter_byte       [F[_] : Applicative]  : Converter[F, Byte, Text]         = toText
  implicit def text_converter_short      [F[_] : Applicative]  : Converter[F, Short, Text]        = toText
  implicit def text_converter_char       [F[_] : Applicative]  : Converter[F, Char, Text]         = toText
  implicit def text_converter_int        [F[_] : Applicative]  : Converter[F, Int, Text]          = toText
  implicit def text_converter_long       [F[_] : Applicative]  : Converter[F, Long, Text]         = toText
  implicit def text_converter_float      [F[_] : Applicative]  : Converter[F, Float, Text]        = toText
  implicit def text_converter_double     [F[_] : Applicative]  : Converter[F, Double, Text]       = toText

  implicit def string_converter_text     [F[_] : MonadEx]  : Converter[F, Text, String] =
    toT(Success(_))
  implicit def byte_converter_text       [F[_] : MonadEx]  : Converter[F, Text, Byte]   =
    toT(v => Try(v.toByte))
  implicit def char_converter_text       [F[_] : MonadEx]  : Converter[F, Text, Char]   =
    toT(v => Try(v.toCharArray.apply(0)))
  implicit def short_converter_text      [F[_] : MonadEx]  : Converter[F, Text, Short]  =
    toT(v => Try(v.toShort))
  implicit def int_converter_text        [F[_] : MonadEx]  : Converter[F, Text, Int]    =
    toT(v => Try(v.toInt))
  implicit def long_converter_text       [F[_] : MonadEx]  : Converter[F, Text, Long]  =
    toT(v => Try(v.toLong))
  implicit def float_converter_text      [F[_] : MonadEx]  : Converter[F, Text, Float]  =
    toT(v => Try(v.toFloat))
  implicit def double_converter_text     [F[_] : MonadEx]  : Converter[F, Text, Double] =
    toT(v => Try(v.toDouble))

  
  private def toText[F[_] : Applicative, I] : Converter[F, I, Text] = 
    Converter.of(v => Applicative[F].pure(Text(v.toString)))
  private def toT[F[_] : MonadEx, O](f: String => Try[O]) : Converter[F, Text, O]    =
    Converter.of(t => MonadEx[F].fromTry(Try(t.text).flatMap(f)))
  // format: on
}
