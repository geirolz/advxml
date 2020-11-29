package advxml.instances

import advxml.core.{=:!=, ErrorHandler, ExHandler}
import advxml.core.data._
import advxml.core.utils.XmlUtils
import cats.{Applicative, FlatMap, Id, Monad}

import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[instances] trait ConverterInstances {

  implicit def identityConverter[F[_]: Applicative, A, B: A =:= *]: Converter[F, A, A] =
    Converter.id[F, A]

  implicit def convStringToAsConvTextTo[F[_]: FlatMap, T: * =:!= Text](implicit
    c: StringTo[F, T]
  ): Converter[F, Text, T] =
    c.local(_.text)

  implicit def convToStringAsToText[F[_]: FlatMap, T: * =:!= Text](implicit
    c: ToString[F, T]
  ): Converter[F, T, Text] =
    c.map(Text(_))

  implicit def node_to_elem: PureConverter[Node, Elem] =
    PureConverter.of(XmlUtils.nodeToElem)

  import cats.instances.try_._

  // format: off
  implicit val id_bigInt_to_str     : ToString[Id, BigInt     ] = toString
  implicit val id_bigDecimal_to_str : ToString[Id, BigDecimal ] = toString
  implicit val id_byte_to_str       : ToString[Id, Byte       ] = toString
  implicit val id_short_to_str      : ToString[Id, Short      ] = toString
  implicit val id_char_to_str       : ToString[Id, Char       ] = toString
  implicit val id_int_to_str        : ToString[Id, Int        ] = toString
  implicit val id_long_to_str       : ToString[Id, Long       ] = toString
  implicit val id_float_to_str      : ToString[Id, Float      ] = toString
  implicit val id_double_to_str     : ToString[Id, Double     ] = toString

  implicit val id_str_to_bigInt     : StringTo[Id, BigInt     ] = liftPure[Try, String, BigInt].map(_.get)
  implicit val id_str_to_bigDecimal : StringTo[Id, BigDecimal ] = liftPure[Try, String, BigDecimal].map(_.get)
  implicit val id_str_to_byte       : StringTo[Id, Byte       ] = liftPure[Try, String, Byte].map(_.get)
  implicit val id_str_to_char       : StringTo[Id, Char       ] = liftPure[Try, String, Char].map(_.get)
  implicit val id_str_to_short      : StringTo[Id, Short      ] = liftPure[Try, String, Short].map(_.get)
  implicit val id_str_to_int        : StringTo[Id, Int        ] = liftPure[Try, String, Int].map(_.get)
  implicit val id_str_to_long       : StringTo[Id, Long       ] = liftPure[Try, String, Long].map(_.get)
  implicit val id_str_to_float      : StringTo[Id, Float      ] = liftPure[Try, String, Float].map(_.get)
  implicit val id_str_to_double     : StringTo[Id, Double     ] = liftPure[Try, String, Double].map(_.get)

  //MONAD ERROR
  implicit def monad_str_to_bigInt    [F[_] : Monad : ExHandler]: StringTo[F, BigInt    ] = fromString(BigInt(_))
  implicit def monad_str_to_bigDecimal[F[_] : Monad : ExHandler]: StringTo[F, BigDecimal] = fromString(BigDecimal(_))
  implicit def monad_str_to_byte      [F[_] : Monad : ExHandler]: StringTo[F, Byte      ] = fromString(_.toByte)
  implicit def monad_str_to_char      [F[_] : Monad : ExHandler]: StringTo[F, Char      ] = fromString(_.toCharArray.apply(0))
  implicit def monad_str_to_short     [F[_] : Monad : ExHandler]: StringTo[F, Short     ] = fromString(_.toShort)
  implicit def monad_str_to_int       [F[_] : Monad : ExHandler]: StringTo[F, Int       ] = fromString(_.toInt)
  implicit def monad_str_to_long      [F[_] : Monad : ExHandler]: StringTo[F, Long      ] = fromString(_.toLong)
  implicit def monad_str_to_float     [F[_] : Monad : ExHandler]: StringTo[F, Float     ] = fromString(_.toFloat)
  implicit def monad_str_to_double    [F[_] : Monad : ExHandler]: StringTo[F, Double    ] = fromString(_.toDouble)

  // format: on

  private def toString[I]: ToString[Id, I] =
    PureConverter.of(v => v.toString)

  private def fromString[F[_]: Monad: ExHandler, O](f: String => O): StringTo[F, O] =
    Converter.of(s => ErrorHandler.fromTry(Try(f(s))))

  private def liftPure[F[_], A, B](implicit c: Converter[F, A, B]): PureConverter[A, F[B]] =
    PureConverter.of(a => c.lift[Id].apply(a))
}
