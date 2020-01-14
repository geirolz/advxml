package advxml.instances

import advxml.core.convert.{Converter, PureConverter}
import advxml.core.validate.MonadEx
import cats.Applicative

import scala.util.Try
import scala.xml.Text

private[instances] trait ConvertersInstances extends CommonConvertersInstances with TextConverterInstances

private[instances] sealed trait CommonConvertersInstances {
  implicit def identityConverter[F[_]: Applicative, A]: Converter[F, A, A] = Converter.id[F, A]
  implicit def pureIdentityConverter[A]: PureConverter[A, A] = PureConverter.id[A]
}

private[instances] sealed trait TextConverterInstances {

  // format: off
  private def toText[F[_] : Applicative, I] : Converter[F, I, Text] =
    Converter.of(v => Applicative[F].pure(Text(v.toString)))
  private def fromText[F[_] : MonadEx, O](f: String => O) : Converter[F, Text, O]    =
    Converter.of(t => MonadEx[F].fromTry(Try(t.text).flatMap(v => Try(f(v)))))
  
  implicit def string_to_text      [F[_] : Applicative] : Converter[F, String     , Text] = toText
  implicit def bigInt_to_text      [F[_] : Applicative] : Converter[F, BigInt     , Text] = toText
  implicit def bigDecimal_to_text  [F[_] : Applicative] : Converter[F, BigDecimal , Text] = toText
  implicit def byte_to_text        [F[_] : Applicative] : Converter[F, Byte       , Text] = toText
  implicit def short_to_text       [F[_] : Applicative] : Converter[F, Short      , Text] = toText
  implicit def char_to_text        [F[_] : Applicative] : Converter[F, Char       , Text] = toText
  implicit def int_to_text         [F[_] : Applicative] : Converter[F, Int        , Text] = toText
  implicit def long_to_text        [F[_] : Applicative] : Converter[F, Long       , Text] = toText
  implicit def float_to_text       [F[_] : Applicative] : Converter[F, Float      , Text] = toText
  implicit def double_to_text      [F[_] : Applicative] : Converter[F, Double     , Text] = toText

  implicit def text_to_string      [F[_] : MonadEx] : Converter[F, Text, String     ] = fromText(identity)
  implicit def text_to_bigInt      [F[_] : MonadEx] : Converter[F, Text, BigInt     ] = fromText(BigInt(_))
  implicit def text_to_bigDecimal  [F[_] : MonadEx] : Converter[F, Text, BigDecimal ] = fromText(BigDecimal(_))
  implicit def text_to_byte        [F[_] : MonadEx] : Converter[F, Text, Byte       ] = fromText(_.toByte)
  implicit def text_to_char        [F[_] : MonadEx] : Converter[F, Text, Char       ] = fromText(_.toCharArray.apply(0))
  implicit def text_to_short       [F[_] : MonadEx] : Converter[F, Text, Short      ] = fromText(_.toShort)
  implicit def text_to_int         [F[_] : MonadEx] : Converter[F, Text, Int        ] = fromText(_.toInt)
  implicit def text_to_long        [F[_] : MonadEx] : Converter[F, Text, Long       ] = fromText(_.toLong)
  implicit def text_to_float       [F[_] : MonadEx] : Converter[F, Text, Float      ] = fromText(_.toFloat)
  implicit def text_to_double      [F[_] : MonadEx] : Converter[F, Text, Double     ] = fromText(_.toDouble)
  // format: on
}
