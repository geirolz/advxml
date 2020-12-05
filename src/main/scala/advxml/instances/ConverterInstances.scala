package advxml.instances

import advxml.core.{=:!=, ErrorHandler, ExHandler}
import advxml.core.data._
import advxml.core.utils.XmlUtils
import cats.{Applicative, Functor, Id, Monad}

import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[instances] trait ConverterInstances
    extends ConverterLowPriorityImplicitsLessGeneric
    with ConverterLowPriorityImplicitsMostGeneric {

  implicit val nodeToElemConverter: PureConverter[Node, Elem] =
    PureConverter.of(XmlUtils.nodeToElem)

  // format: off
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

  private def liftPure[F[_], A, B](implicit c: Converter[F, A, B]): PureConverter[A, F[B]] =
    PureConverter.of(a => c.lift[Id].apply(a))

  private def fromString[F[_]: Monad: ExHandler, O](f: String => O): StringTo[F, O] =
    Converter.of(s => ErrorHandler.fromTry(Try(f(s))))
}

private sealed trait ConverterLowPriorityImplicitsLessGeneric {

  implicit def toStringConverter[F[_]: Applicative, T: * =:!= String]: ToString[F, T] =
    Converter.of(a => Applicative[F].pure(a.toString))

  implicit def convStringToAsConvTextTo[F[_], T: * =:!= Text](implicit
    c: Converter[F, String, T]
  ): Converter[F, Text, T] =
    c.local(_.text)

  implicit def convToStringAsToText[F[_]: Functor, T: * =:!= Text](implicit
    c: Converter[F, T, String]
  ): Converter[F, T, Text] =
    c.map(Text(_))
}

private sealed trait ConverterLowPriorityImplicitsMostGeneric {

  implicit def identityConverter[F[_]: Applicative, A, B: A =:= *]: Converter[F, A, A] =
    Converter.id[F, A]
}
