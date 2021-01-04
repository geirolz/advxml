package advxml.instances

import advxml.core.{=:!=, AppExOrEu}
import advxml.core.data._
import advxml.core.transform.XmlContentZoomRunner
import advxml.core.utils.XmlUtils
import cats.{~>, Applicative, FlatMap, Semigroup}
import cats.data.{NonEmptyList, Validated}
import advxml.core.data.ValidationRule

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
        name = "NonEmpty",
        validator = _.nonEmpty,
        errorReason = "Empty value"
      )

  /** Check if value match specified regex.
    * @param regex instance.
    */
  case class MatchRegex(regex: Regex)
      extends ValidationRule(
        name = "MatchRegex",
        validator = v => regex.findFirstMatchIn(v).isDefined,
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

  implicit def identityConverterApplicative[F[_]: Applicative, A: * =:!= F[A]]: Converter[A, F[A]] =
    Converter.idF[F, A]
}

private sealed trait ConverterLowerPriorityImplicits1 {

  import cats.syntax.all._

  implicit def deriveTextToF_fromValueToF[F[_], T: * =:!= Text](implicit
    c: SimpleValue As F[T]
  ): Text As F[T] =
    c.local(t => SimpleValue(t.data))

  implicit def deriveTAsText_fromTAsValue[T: * =:!= Text](implicit
    c: T As SimpleValue
  ): T As Text =
    c.map(v => Text(v.get))

  implicit def deriveTAsText_fromTAsValidatedValue[F[_]: AppExOrEu, T: * =:!= Text](implicit
    c: T As ValidatedValue
  ): T As F[Text] =
    c.map(v => v.extract[F].map(Text(_)))

  implicit def converterFlatMapAs[F[_]: FlatMap, A, B](implicit c: Converter[A, F[B]]): Converter[F[A], F[B]] =
    Converter.of(fa => fa.flatMap(a => c.run(a)))

  implicit def converterAndThenAs[E, A, B](implicit
    c: Converter[A, Validated[E, B]]
  ): Converter[Validated[E, A], Validated[E, B]] =
    Converter.of(fa => fa.andThen(a => c.run(a)))
}

private sealed trait ConverterLowerPriorityImplicits2 {

  //=============================== Node ===============================
  implicit val nodeToElemConverter: Node As Elem =
    Converter.of(XmlUtils.nodeToElem)

  //=============================== Throwable ===============================
  implicit val converterThrowableNelToThrowableEx: ThrowableNel As Throwable =
    Converter.of(ThrowableNel.toThrowable)

  implicit val converterThrowableToThrowableNel: Throwable As ThrowableNel =
    Converter.of(ThrowableNel.fromThrowable)

  //=============================== Value ===============================
  implicit val convertStringToValue: String As SimpleValue =
    Converter.of(SimpleValue(_))

  implicit val convertValueToString: SimpleValue As String =
    Converter.of(a => a.get)

  implicit def converterXmlContentZoomRunnerForValidated[A](implicit
    c: Converter[ValidatedNelEx[String], ValidatedNelEx[A]]
  ): Converter[XmlContentZoomRunner, ValidatedNelEx[A]] =
    Converter.of(r => c.run(r.extractAsValidated))

  implicit def converterXmlContentZoomRunnerForAppExOrEu[F[_]: AppExOrEu: FlatMap, A](implicit
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

  implicit def convertValueToFString    [F[_] : AppExOrEu] : Value As F[String    ] = fromBox(a => a)
  implicit def convertValueToFBigInt    [F[_] : AppExOrEu] : Value As F[BigInt    ] = fromBox(BigInt(_))
  implicit def convertValueToFBigDecimal[F[_] : AppExOrEu] : Value As F[BigDecimal] = fromBox(BigDecimal(_))
  implicit def convertValueToFNyte      [F[_] : AppExOrEu] : Value As F[Byte      ] = fromBox(_.toByte)
  implicit def convertValueToFChar      [F[_] : AppExOrEu] : Value As F[Char      ] = fromBox(_.toCharArray.apply(0))
  implicit def convertValueToFShort     [F[_] : AppExOrEu] : Value As F[Short     ] = fromBox(_.toShort)
  implicit def convertValueToFInt       [F[_] : AppExOrEu] : Value As F[Int       ] = fromBox(_.toInt)
  implicit def convertValueToFLong      [F[_] : AppExOrEu] : Value As F[Long      ] = fromBox(_.toLong)
  implicit def convertValueToFFloat     [F[_] : AppExOrEu] : Value As F[Float     ] = fromBox(_.toFloat)
  implicit def convertValueToFDouble    [F[_] : AppExOrEu] : Value As F[Double    ] = fromBox(_.toDouble)
  // format: on

  private def toValue[T]: Converter[T, SimpleValue] = Converter.of[T, SimpleValue](t => SimpleValue(t.toString))

  private def fromBox[F[_]: AppExOrEu, O](f: String => O): Converter[Value, F[O]] =
    Converter.of { b => AppExOrEu.fromTry(b.extract[Try].flatMap(v => Try(f(v)))) }
}

private sealed trait ConverterNaturalTransformationInstances {

  //APP EX
  implicit def appExOrEuTryNatTransformationInstance[G[_]: AppExOrEu]: Try ~> G =
    λ[Try ~> G](AppExOrEu.fromTry(_))

  implicit def appExOrEuEitherExNatTransformationInstance[G[_]: AppExOrEu]: EitherEx ~> G =
    λ[EitherEx ~> G](AppExOrEu.fromEitherEx(_))

  implicit def appExOrEuEitherNelExNatTransformationInstance[G[_]: AppExOrEu]: EitherNelEx ~> G =
    λ[EitherNelEx ~> G](AppExOrEu.fromEitherNelEx(_))

  implicit def appExOrEuValidatedExNatTransformationInstance[G[_]: AppExOrEu]: ValidatedEx ~> G =
    λ[ValidatedEx ~> G](AppExOrEu.fromValidatedEx(_))

  implicit def appExOrEuValidatedNelExNatTransformationInstance[G[_]: AppExOrEu]: ValidatedNelEx ~> G =
    λ[ValidatedNelEx ~> G](AppExOrEu.fromValidatedNelEx(_))
}
