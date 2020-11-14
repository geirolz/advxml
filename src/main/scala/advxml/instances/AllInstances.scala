package advxml.instances
import advxml.core.=:!=
import advxml.core.{Converter, PureConverter, ThrowableNel}
import advxml.core.exceptions.AggregatedException
import advxml.core.utils.XmlUtils
import advxml.core.MonadEx
import cats.data.Validated.{Invalid, Valid}
import cats.kernel.Semigroup
import cats.{Applicative, FlatMap, Id, MonadError}
import cats.data.{NonEmptyList, Validated}

import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[advxml] trait AllInstances
    extends AllCommonInstances
    with AllXmlTransformerInstances
    with AllXmlTraverserInstances

private[instances] trait AllCommonInstances
    extends ConverterInstances
    with ValidationInstances
    with AggregatedExceptionInstances

//********************************* CONVERTERS **********************************
private[instances] trait ConverterInstances {

  implicit def identityConverter[F[_]: Applicative, A]: Converter[F, A, A] =
    Converter.id[F, A]

  implicit def convertersCombinatorForTextOps[F[_]: FlatMap, A: * =:!= Text: * =:!= C, C: * =:!= Text](implicit
    c1: Converter[F, A, Text],
    c2: Converter[F, Text, C]
  ): Converter[F, A, C] =
    c1.andThen(c2)

  implicit def node_to_elem: PureConverter[Node, Elem] =
    PureConverter.of(XmlUtils.nodeToElem)

  import cats.instances.try_._

  // format: off
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

  private def toText[I]: PureConverter[I, Text] =
    PureConverter.of(v => Text(v.toString))

  private def textTo[F[_]: MonadEx, O](f: String => O): Converter[F, Text, O] =
    Converter.of(t => MonadEx[F].fromTry(Try(t.text).flatMap(v => Try(f(v)))))

  private def liftPure[F[_], A, B](implicit c: Converter[F, A, B]): PureConverter[A, F[B]] =
    PureConverter.of(a => c.lift[Id].apply(a))
}

//********************************* VALIDATION **********************************
private[instances] trait ValidationInstances {

  implicit val throwable_to_ThrowableNel: Throwable => ThrowableNel = {
    case ex: AggregatedException => ex.exceptions
    case ex                      => NonEmptyList.one(ex)
  }

  implicit val throwableNel_to_Throwable: ThrowableNel => Throwable = nelE => new AggregatedException(nelE)

  implicit def validatedMonadErrorInstance[E1, E2](implicit
    toE1: E2 => E1,
    toE2: E1 => E2
  ): MonadError[Validated[E1, *], E2] =
    new MonadError[Validated[E1, *], E2] {

      def raiseError[A](e: E2): Validated[E1, A] = Invalid(toE1(e))

      def pure[A](x: A): Validated[E1, A] = Valid(x)

      def handleErrorWith[A](fa: Validated[E1, A])(f: E2 => Validated[E1, A]): Validated[E1, A] =
        fa match {
          case Valid(_)   => fa
          case Invalid(e) => f(toE2(e))
        }

      def flatMap[A, B](fa: Validated[E1, A])(f: A => Validated[E1, B]): Validated[E1, B] =
        fa match {
          case Valid(a)       => f(a)
          case i @ Invalid(_) => i
        }

      @scala.annotation.tailrec
      def tailRecM[A, B](a: A)(f: A => Validated[E1, Either[A, B]]): Validated[E1, B] =
        f(a) match {
          case Valid(eitherAb) =>
            eitherAb match {
              case Right(b) => Valid(b)
              case Left(a)  => tailRecM(a)(f)
            }
          case i @ Invalid(_) => i
        }
    }
}

//********************************* EXCEPTIONS **********************************
private[instances] trait AggregatedExceptionInstances {
  implicit lazy val semigroupInstanceForAggregatedException: Semigroup[Throwable] =
    (x: Throwable, y: Throwable) => new AggregatedException(NonEmptyList.of(x, y))
}
