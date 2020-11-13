package advxml.instances

import advxml.=:!=
import advxml.core.convert.{Converter, PureConverter}
import advxml.core.utils.XmlUtils
import advxml.core.validate.MonadEx
import cats.{Applicative, FlatMap, Id}

import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[instances] trait ConvertersInstances extends CommonConvertersInstances with TextConverterInstances

private[instances] sealed trait CommonConvertersInstances {

  implicit def identityConverter[F[_]: Applicative, A]: Converter[F, A, A] =
    Converter.id[F, A]

  implicit def convertersCombinatorForTextOps[F[_]: FlatMap, A: * =:!= Text: * =:!= C, C: * =:!= Text](implicit
    c1: Converter[F, A, Text],
    c2: Converter[F, Text, C]
  ): Converter[F, A, C] =
    c1.andThen(c2)

  implicit def node_to_elem: PureConverter[Node, Elem] =
    PureConverter.of(XmlUtils.nodeToElem)
}

private[instances] sealed trait TextConverterInstances {

  // format: off
  private def toText[I] : PureConverter[I, Text] =
    PureConverter.of(v => Text(v.toString))
  
  private def textTo[F[_] : MonadEx, O](f: String => O) : Converter[F, Text, O]    =
    Converter.of(t => MonadEx[F].fromTry(Try(t.text).flatMap(v => Try(f(v)))))
  
  private def liftPure[F[_], A, B](implicit c: Converter[F, A, B]) : PureConverter[A, F[B]] = 
    PureConverter.of(a => c.lift[Id].apply(a))
  
  import cats.instances.try_._

  implicit val id_string_to_text     : PureConverter[String     , Text] = toText
  implicit val id_bigInt_to_text     : PureConverter[BigInt     , Text] = toText
  implicit val id_bigDecimal_to_text : PureConverter[BigDecimal , Text] = toText
  implicit val id_byte_to_text       : PureConverter[Byte       , Text] = toText
  implicit val id_short_to_text      : PureConverter[Short      , Text] = toText
  implicit val id_char_to_text       : PureConverter[Char       , Text] = toText
  implicit val id_int_to_text        : PureConverter[Int        , Text] = toText
  implicit val id_long_to_text       : PureConverter[Long       , Text] = toText
  implicit val id_float_to_text      : PureConverter[Float      , Text] = toText
  implicit val id_double_to_text     : PureConverter[Double     , Text] = toText
  
  implicit val id_text_to_string      : PureConverter[Text, String     ] = liftPure[Try, Text, String     ].map(_.get)
  implicit val id_text_to_bigInt      : PureConverter[Text, BigInt     ] = liftPure[Try, Text, BigInt     ].map(_.get)
  implicit val id_text_to_bigDecimal  : PureConverter[Text, BigDecimal ] = liftPure[Try, Text, BigDecimal ].map(_.get)
  implicit val id_text_to_byte        : PureConverter[Text, Byte       ] = liftPure[Try, Text, Byte       ].map(_.get)
  implicit val id_text_to_char        : PureConverter[Text, Char       ] = liftPure[Try, Text, Char       ].map(_.get)
  implicit val id_text_to_short       : PureConverter[Text, Short      ] = liftPure[Try, Text, Short      ].map(_.get)
  implicit val id_text_to_int         : PureConverter[Text, Int        ] = liftPure[Try, Text, Int        ].map(_.get)
  implicit val id_text_to_long        : PureConverter[Text, Long       ] = liftPure[Try, Text, Long       ].map(_.get)
  implicit val id_text_to_float       : PureConverter[Text, Float      ] = liftPure[Try, Text, Float      ].map(_.get)
  implicit val id_text_to_double      : PureConverter[Text, Double     ] = liftPure[Try, Text, Double     ].map(_.get)
  
  //MONAD ERROR
  implicit def monad_text_to_string      [F[_] : MonadEx] : Converter[F, Text, String     ] = textTo(identity)
  implicit def monad_text_to_bigInt      [F[_] : MonadEx] : Converter[F, Text, BigInt     ] = textTo(BigInt(_))
  implicit def monad_text_to_bigDecimal  [F[_] : MonadEx] : Converter[F, Text, BigDecimal ] = textTo(BigDecimal(_))
  implicit def monad_text_to_byte        [F[_] : MonadEx] : Converter[F, Text, Byte       ] = textTo(_.toByte)
  implicit def monad_text_to_char        [F[_] : MonadEx] : Converter[F, Text, Char       ] = textTo(_.toCharArray.apply(0))
  implicit def monad_text_to_short       [F[_] : MonadEx] : Converter[F, Text, Short      ] = textTo(_.toShort)
  implicit def monad_text_to_int         [F[_] : MonadEx] : Converter[F, Text, Int        ] = textTo(_.toInt)
  implicit def monad_text_to_long        [F[_] : MonadEx] : Converter[F, Text, Long       ] = textTo(_.toLong)
  implicit def monad_text_to_float       [F[_] : MonadEx] : Converter[F, Text, Float      ] = textTo(_.toFloat)
  implicit def monad_text_to_double      [F[_] : MonadEx] : Converter[F, Text, Double     ] = textTo(_.toDouble)
  // format: on
}
