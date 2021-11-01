package advxml.instances

import advxml.core.{=:!=, ApplicativeThrowOrEu}
import advxml.core.data._
import advxml.core.transform.XmlContentZoomRunner
import advxml.core.utils.XmlUtils
import cats.{~>, Applicative, FlatMap, Semigroup}
import cats.data.{NonEmptyList, Validated}
import advxml.core.data.ValidationRule

import scala.annotation.unused
import scala.util.matching.Regex
import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[instances] trait AllDataInstances
    extends AllValueInstances
    with AggregatedExceptionInstances
    with AllConverterInstances

//========================= VALUE =========================
private[instances] trait AllValueInstances {

  /** Check if value is empty.
    */
  case object NonEmpty
      extends ValidationRule(
        name        = "NonEmpty",
        validator   = _.nonEmpty,
        errorReason = "Empty value"
      )

  /** Check if value match specified regex.
    * @param regex
    *   instance.
    */
  case class MatchRegex(regex: Regex)
      extends ValidationRule(
        name        = "MatchRegex",
        validator   = v => regex.findFirstMatchIn(v).isDefined,
        errorReason = s"No match with regex [$regex]"
      )
}

//============================== EXCEPTIONS ==============================
private trait AggregatedExceptionInstances {
  implicit val semigroupInstanceForAggregatedException: Semigroup[Throwable] =
    (x: Throwable, y: Throwable) => ThrowableNel.toThrowable(NonEmptyList.of(x, y))
}

//========================= CONVERTERS =========================
private[instances] trait AllConverterInstances
    extends ConverterLowerPriorityImplicits1
    with ConverterLowerPriorityImplicits2
    with ConverterNaturalTransformationInstances {

  implicit def identityConverter[A]: Converter[A, A] = Converter.id[A]

  implicit def identityConverterApplicative[F[_], A](implicit
    F: Applicative[F],
    @unused notFA: A =:!= F[A]
  ): Converter[A, F[A]] =
    Converter.of[A, F[A]](F.pure)
}

private sealed trait ConverterLowerPriorityImplicits1 {

  import cats.syntax.all._

  implicit def deriveTextToF_fromValueToF[F[_], T](implicit
    c: SimpleValue As F[T],
    @unused notText: T =:!= Text
  ): Text As F[T] =
    c.contramap(t => SimpleValue(t.data))

  implicit def deriveTAsText_fromTAsValue[T](implicit
    c: T As SimpleValue,
    @unused notText: T =:!= Text
  ): T As Text =
    c.map(v => Text(v.get))

  implicit def deriveTAsText_fromTAsValidatedValue[F[_], T](implicit
    c: T As ValidatedValue,
    a: ApplicativeThrowOrEu[F],
    @unused notText: T =:!= Text
  ): T As F[Text] =
    c.map(v => v.extract[F].map(Text(_)))

  implicit def converterFlatMapAs[F[_]: FlatMap, A, B](implicit
    c: Converter[A, F[B]]
  ): Converter[F[A], F[B]] =
    Converter.of(fa => fa.flatMap(a => c.run(a)))

  implicit def converterAndThenAs[E, A, B](implicit
    c: Converter[A, Validated[E, B]]
  ): Converter[Validated[E, A], Validated[E, B]] =
    Converter.of(fa => fa.andThen(a => c.run(a)))
}

private sealed trait ConverterLowerPriorityImplicits2 {

  // =============================== Node ===============================
  implicit val nodeToElemConverter: Node As Elem =
    Converter.of(XmlUtils.nodeToElem)

  // =============================== Throwable ===============================
  implicit val converterThrowableNelToThrowableEx: ThrowableNel As Throwable =
    Converter.of(ThrowableNel.toThrowable)

  implicit val converterThrowableToThrowableNel: Throwable As ThrowableNel =
    Converter.of(ThrowableNel.fromThrowable)

  // =============================== Value ===============================
  implicit val convertStringToValue: String As SimpleValue =
    Converter.of(SimpleValue(_))

  implicit val convertValueToString: SimpleValue As String =
    Converter.of(a => a.get)

  implicit def converterXmlContentZoomRunnerForValidated[A](implicit
    c: Converter[ValidatedNelThrow[String], ValidatedNelThrow[A]]
  ): Converter[XmlContentZoomRunner, ValidatedNelThrow[A]] =
    Converter.of(r => c.run(r.validated))

  implicit def converterXmlContentZoomRunnerForApplicativeThrowOrEu[F[
    _
  ]: ApplicativeThrowOrEu: FlatMap, A](implicit
    c: Converter[F[String], F[A]]
  ): Converter[XmlContentZoomRunner, F[A]] =
    Converter.of(r => c.run(r.extract[F]))

  // format: off
  implicit val convertBigIntToValue     : BigInt     As SimpleValue = toValue
  implicit val convertBigDecimalToValue : BigDecimal As SimpleValue = toValue
  implicit val convertNyteToValue       : Byte       As SimpleValue = toValue
  implicit val convertCharToValue       : Char       As SimpleValue = toValue
  implicit val convertShortToValue      : Short      As SimpleValue = toValue
  implicit val convertIntToValue        : Int        As SimpleValue = toValue
  implicit val convertLongToValue       : Long       As SimpleValue = toValue
  implicit val convertFloatToValue      : Float      As SimpleValue = toValue
  implicit val convertDoubleToValue     : Double     As SimpleValue = toValue

  implicit def convertValueToFString    [F[_] : ApplicativeThrowOrEu] : Value As F[String    ] = fromBox(a => a)
  implicit def convertValueToFBigInt    [F[_] : ApplicativeThrowOrEu] : Value As F[BigInt    ] = fromBox(BigInt(_))
  implicit def convertValueToFBigDecimal[F[_] : ApplicativeThrowOrEu] : Value As F[BigDecimal] = fromBox(BigDecimal(_))
  implicit def convertValueToFNyte      [F[_] : ApplicativeThrowOrEu] : Value As F[Byte      ] = fromBox(_.toByte)
  implicit def convertValueToFChar      [F[_] : ApplicativeThrowOrEu] : Value As F[Char      ] = fromBox(_.toCharArray.apply(0))
  implicit def convertValueToFShort     [F[_] : ApplicativeThrowOrEu] : Value As F[Short     ] = fromBox(_.toShort)
  implicit def convertValueToFInt       [F[_] : ApplicativeThrowOrEu] : Value As F[Int       ] = fromBox(_.toInt)
  implicit def convertValueToFLong      [F[_] : ApplicativeThrowOrEu] : Value As F[Long      ] = fromBox(_.toLong)
  implicit def convertValueToFFloat     [F[_] : ApplicativeThrowOrEu] : Value As F[Float     ] = fromBox(_.toFloat)
  implicit def convertValueToFDouble    [F[_] : ApplicativeThrowOrEu] : Value As F[Double    ] = fromBox(_.toDouble)
  // format: on

  private def toValue[T]: Converter[T, SimpleValue] =
    Converter.of[T, SimpleValue](t => SimpleValue(t.toString))

  private def fromBox[F[_]: ApplicativeThrowOrEu, O](f: String => O): Converter[Value, F[O]] =
    Converter.of { b => ApplicativeThrowOrEu.fromTry(b.extract[Try].flatMap(v => Try(f(v)))) }
}

private sealed trait ConverterNaturalTransformationInstances {

  // APP EX
  implicit def ApplicativeThrowOrEuTryNatTransformationInstance[G[_]: ApplicativeThrowOrEu]
    : Try ~> G =
    new (Try ~> G) { def apply[A](a: Try[A]): G[A] = ApplicativeThrowOrEu.fromTry(a) }

  implicit def ApplicativeThrowOrEuEitherThrowNatTransformationInstance[G[_]: ApplicativeThrowOrEu]
    : EitherThrow ~> G =
    new (EitherThrow ~> G) {
      def apply[A](a: EitherThrow[A]): G[A] = ApplicativeThrowOrEu.fromEitherThrow(a)
    }

  implicit def ApplicativeThrowOrEuEitherNelThrowNatTransformationInstance[G[
    _
  ]: ApplicativeThrowOrEu]: EitherNelThrow ~> G =
    new (EitherNelThrow ~> G) {
      def apply[A](a: EitherNelThrow[A]): G[A] = ApplicativeThrowOrEu.fromEitherNelThrow(a)
    }

  implicit def ApplicativeThrowOrEuValidatedThrowNatTransformationInstance[G[
    _
  ]: ApplicativeThrowOrEu]: ValidatedThrow ~> G =
    new (ValidatedThrow ~> G) {
      def apply[A](a: ValidatedThrow[A]): G[A] = ApplicativeThrowOrEu.fromValidatedThrow(a)
    }

  implicit def ApplicativeThrowOrEuValidatedNelThrowNatTransformationInstance[G[
    _
  ]: ApplicativeThrowOrEu]: ValidatedNelThrow ~> G =
    new (ValidatedNelThrow ~> G) {
      def apply[A](a: ValidatedNelThrow[A]): G[A] = ApplicativeThrowOrEu.fromValidatedNelThrow(a)
    }
}
