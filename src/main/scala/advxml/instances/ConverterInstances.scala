package advxml.instances

import advxml.core.{=:!=, AppExOrEu}
import advxml.core.data._
import advxml.core.transform.XmlContentZoomRunner
import advxml.core.utils.XmlUtils
import cats.{~>, Applicative, FlatMap}
import cats.data.Validated

import scala.util.Try
import scala.xml.{Elem, Node, Text}

private[instances] trait ConverterInstances
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
    c: Value As F[T]
  ): Text As F[T] =
    c.local(t => Value(t.data))

  implicit def deriveTAsText_fromTAsValue[T: * =:!= Text](implicit
    c: T As Value
  ): T As Text =
    c.map(v => Text(v.unboxed))

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

  import cats.syntax.functor._

  //=============================== Node ===============================
  implicit val nodeToElemConverter: Node As Elem =
    Converter.of(XmlUtils.nodeToElem)

  //=============================== Throwable ===============================
  implicit val converterThrowableNelToThrowableEx: ThrowableNel As Throwable =
    Converter.of(ThrowableNel.toThrowable)

  implicit val converterThrowableToThrowableNel: Throwable As ThrowableNel =
    Converter.of(ThrowableNel.fromThrowable)

  //=============================== Value ===============================
  implicit val convertStringToValue: String As Value =
    Converter.of(Value(_))

  implicit def convertValueToString[T: * =:= Value: * =:!= ValidatedValue]: T As String =
    Converter.of(a => a.unboxed)

  implicit def converterXmlContentZoomRunnerForValidated[A](implicit
    c: Converter[ValidatedNelEx[String], ValidatedNelEx[A]]
  ): Converter[XmlContentZoomRunner, ValidatedNelEx[A]] =
    Converter.of(r => c.run(r.extractAsValidated))

  implicit def converterXmlContentZoomRunnerForAppExOrEu[F[_]: AppExOrEu: FlatMap, A](implicit
    c: Converter[F[String], F[A]]
  ): Converter[XmlContentZoomRunner, F[A]] =
    Converter.of(r => c.run(r.extract[F]))

  // format: off
  implicit val convertBigIntToValue     : BigInt     As Value = toValue
  implicit val convertBigDecimalToValue : BigDecimal As Value = toValue
  implicit val convertNyteToValue       : Byte       As Value = toValue
  implicit val convertCharToValue       : Char       As Value = toValue
  implicit val convertShortToValue      : Short      As Value = toValue
  implicit val convertIntToValue        : Int        As Value = toValue
  implicit val convertLongToValue       : Long       As Value = toValue
  implicit val convertFloatToValue      : Float      As Value = toValue
  implicit val convertDoubleToValue     : Double     As Value = toValue

  implicit def convertValueToFString    [F[_] : AppExOrEu] : Value As F[String    ] = fromValue(a => a)
  implicit def convertValueToFBigInt    [F[_] : AppExOrEu] : Value As F[BigInt    ] = fromValue(BigInt(_))
  implicit def convertValueToFBigDecimal[F[_] : AppExOrEu] : Value As F[BigDecimal] = fromValue(BigDecimal(_))
  implicit def convertValueToFNyte      [F[_] : AppExOrEu] : Value As F[Byte      ] = fromValue(_.toByte)
  implicit def convertValueToFChar      [F[_] : AppExOrEu] : Value As F[Char      ] = fromValue(_.toCharArray.apply(0))
  implicit def convertValueToFShort     [F[_] : AppExOrEu] : Value As F[Short     ] = fromValue(_.toShort)
  implicit def convertValueToFInt       [F[_] : AppExOrEu] : Value As F[Int       ] = fromValue(_.toInt)
  implicit def convertValueToFLong      [F[_] : AppExOrEu] : Value As F[Long      ] = fromValue(_.toLong)
  implicit def convertValueToFFloat     [F[_] : AppExOrEu] : Value As F[Float     ] = fromValue(_.toFloat)
  implicit def convertValueToFDouble    [F[_] : AppExOrEu] : Value As F[Double    ] = fromValue(_.toDouble)
  // format: on

  private def toValue[T]: Converter[T, Value] = Converter.of[T, Value](t => Value(t.toString))

  private def fromValue[F[_]: AppExOrEu, O](f: String => O): Converter[Value, F[O]] =
    Converter.of {
      case value: ValidatedValue => value.extract[F].map(f)
      case value: Value          => AppExOrEu.fromTry(Try(f(value.unboxed)))
    }
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
