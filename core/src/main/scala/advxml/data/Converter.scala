package advxml.data

import advxml.{=:!=, ApplicativeThrowOrEu}
import advxml.transform.XmlContentZoomRunner
import advxml.utils.XmlUtils
import cats.{~>, Applicative, FlatMap, Id}
import cats.data.{Kleisli, Validated}
import cats.catsInstancesForId

import scala.annotation.unused
import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq, Text}

/** Advxml Created by geirolad on 31/10/2019.
  *
  * @author
  *   geirolad
  */

/** Represents a function `A => B` to simplify method and class signatures. This alias represent a
  * converter to transform `A` into `B`.
  *
  * @tparam A
  *   Contravariant input object type
  * @tparam B
  *   Output object type
  */
trait Converter[-A, B] {

  def run(a: A): B

  def map[U](f: B => U): Converter[A, U] =
    Converter.of(a => f(run(a)))

  def contramap[AA](f: AA => A): Converter[AA, B] =
    Converter.of(aa => run(f(aa)))

  def asKleisli: Kleisli[Id, A, B] = Kleisli[Id, A, B](run)
}

object Converter extends AllConverterInstances {

  /** Create an instance of [[Converter]]
    * @param f
    *   function to map input to output
    * @tparam A
    *   Input type
    * @tparam B
    *   Output type
    * @return
    *   Converter instance
    */
  def of[A, B](f: A => B): Converter[A, B] = (a: A) => f(a)

  /** Create an always pure converter that return the input instance.
    * @tparam A
    *   input and output type
    * @return
    *   Identity [[Converter]] instance
    */
  def id[A]: Converter[A, A] = Converter.of(a => a)

  /** Create an always pure converter that return the passed value ignoring the converter input.
    * @param b
    *   Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam B
    *   inner output type
    * @return
    *   Constant [[Converter]] instance
    */
  def pure[A, B](b: B): Converter[A, B] = Converter.of(_ => b)

  /** Apply conversion using implicit [[Converter]] instance. This method catch a [[Converter]]
    * instance in the scope that conforms with types `F`, `A` and `B` and then invoke in it the
    * method `apply` passing `a`.
    *
    * @param c
    *   implicit [[Converter]] instance
    * @tparam A
    *   Contravariant input type
    * @tparam B
    *   Output object type
    * @return
    *   Safe conversion of `A` into `B`, express as `F[B]`
    */
  def apply[A, B](implicit c: Converter[A, B]): Converter[A, B] = c
}

private[advxml] trait AllConverterInstances
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

private[advxml] sealed trait ConverterLowerPriorityImplicits1 {

  import cats.syntax.all.*

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

  implicit def deriveStringAsFT_fromSimpleValueAsFT[F[_], T](implicit
    c: SimpleValue As F[T]
  ): String As F[T] =
    c.contramap[String](SimpleValue(_))

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

private[advxml] sealed trait ConverterLowerPriorityImplicits2 {

  // =============================== Node ===============================
  implicit val nodeToElemConverter: Node As Elem =
    Converter.of(XmlUtils.nodeToElem)

  // =============================== Throwable ===============================
  implicit val converterThrowableNelToThrowableEx: ThrowableNel As Throwable =
    Converter.of(ThrowableNel.toThrowable)

  implicit val converterThrowableToThrowableNel: Throwable As ThrowableNel =
    Converter.of(ThrowableNel.fromThrowable)

  // =============================== Value ===============================
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
  implicit val convertStringToValue     : String     As SimpleValue = Converter.of(SimpleValue(_))
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

private[advxml] sealed trait ConverterNaturalTransformationInstances {

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

//=================================== HELPERS ============================================
private[advxml] sealed abstract class FixedBiConverterOps[F[_]: Applicative, C[-A, B] <: Converter[
  A,
  F[B]
]] {
  def of[A, B](f: A => F[B]): C[A, B] = Converter.of[A, F[B]](f).asInstanceOf[C[A, B]]
  def pure[A, B](b: B): C[A, B] =
    Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[C[A, B]]
  def apply[A, B](implicit F: Converter[A, F[B]]): C[A, B] =
    Converter[A, F[B]].asInstanceOf[C[A, B]]
}

private[advxml] sealed abstract class FixedLeftConverterOps[F[_]: Applicative, A, CR[
  B
] <: Converter[A, F[B]]] {
  def of[B](f: A => F[B]): CR[B] = Converter.of[A, F[B]](f).asInstanceOf[CR[B]]
  def pure[B](b: B): CR[B] = Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[CR[B]]
  def apply[B](implicit F: Converter[A, F[B]]): CR[B] = Converter[A, F[B]].asInstanceOf[CR[B]]
}

private[advxml] sealed abstract class FixedRightConverterOps[F[_]: Applicative, B, CL[
  A
] <: Converter[A, F[B]]] {
  def of[A](f: A => F[B]): CL[A] = Converter.of[A, F[B]](f).asInstanceOf[CL[A]]
  def pure[A](b: B): CL[A] = Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[CL[A]]
  def apply[A](implicit F: Converter[A, F[B]]): CL[A] = Converter[A, F[B]].asInstanceOf[CL[A]]
}

object ValidatedConverter extends FixedBiConverterOps[ValidatedNelThrow, ValidatedConverter]

object OptionConverter extends FixedBiConverterOps[Option, OptionConverter]

object XmlDecoder extends FixedLeftConverterOps[ValidatedNelThrow, NodeSeq, XmlDecoder]

object XmlEncoder extends FixedRightConverterOps[Id, NodeSeq, XmlEncoder]
