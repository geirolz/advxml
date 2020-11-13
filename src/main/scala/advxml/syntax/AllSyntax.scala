package advxml.syntax

import advxml.core.validate.{EitherEx, EitherNelEx, MonadEx, MonadNelEx, ValidatedNelEx}
import advxml.core.Predicate
import advxml.core.convert.PureConverter
import advxml.core.transform.actions.{AttributeData, Key, KeyValuePredicate}
import cats.{Alternative, Applicative, Monad}

import scala.util.Try
import scala.xml.Text

private[advxml] trait AllSyntax
    extends AllCommonSyntax
    with XmlTransformerSyntax
    with XmlTraverserSyntax
    with XmlNormalizerSyntax
    with ConvertersSyntax

private[advxml] trait AllCommonSyntax
    extends AttributeSyntax
    with ValidationSyntax
    with NestedMapSyntax
    with PredicateSyntax

//********************************* ATTRIBUTE PREDICATE **********************************
private[syntax] trait AttributeSyntax {

  implicit class KeyStringInterpolationOps(ctx: StringContext) {
    def k(args: Any*): Key = Key(ctx.s(args: _*))
  }

  implicit class AttributeOps(key: Key) {

    def :=[T](v: T)(implicit converter: PureConverter[T, Text]): AttributeData =
      AttributeData(key, converter(v))

    def ===[T](that: T)(implicit converter: PureConverter[T, String]): KeyValuePredicate[String] =
      KeyValuePredicate(key, _ == converter(that))

    def ->[T](valuePredicate: T => Boolean): KeyValuePredicate[T] =
      KeyValuePredicate(key, valuePredicate)

    //********* FOR NUMBERS *********
    def <(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ < _)

    def <=(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ <= _)

    def >(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ > _)

    def >=(that: Double)(implicit converter: PureConverter[String, Double]): KeyValuePredicate[String] =
      buildPredicateForScalaNumber(that, _ >= _)

    private def buildPredicateForScalaNumber(that: Double, p: (Double, Double) => Boolean)(implicit
      converter: PureConverter[String, Double]
    ): KeyValuePredicate[String] =
      key -> (v => p(converter(v), that))
  }
}

//************************************ VALIDATION ****************************************
private[syntax] trait ValidationSyntax {

  implicit class ValidatedExTryOps[A](t: Try[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromTry(t)
  }

  implicit class ValidatedExEitherOps[A](e: EitherEx[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromEither(e)
  }

  implicit class ValidatedExEitherNelOps[A](e: EitherNelEx[A]) {
    def toValidatedEx: ValidatedNelEx[A] = ValidatedNelEx.fromEitherNel(e)
  }

  implicit class ValidatedExOptionOps[A](e: Option[A]) {
    def toValidatedEx(ifNone: => Throwable): ValidatedNelEx[A] = ValidatedNelEx.fromOption(e, ifNone)
  }

  implicit class ValidatedExOps[A](validated: ValidatedNelEx[A]) {

    def transformE[F[_]](implicit F: MonadEx[F]): F[A] =
      ValidatedNelEx.transformE[F, A](validated)(F)

    def transformNE[F[_]](implicit F: MonadNelEx[F]): F[A] =
      ValidatedNelEx.transformNE[F, A](validated)(F)

    def transformA[F[_]](implicit F: Alternative[F]): F[A] =
      ValidatedNelEx.transformA[F, A](validated)(F)
  }
}

//************************************ NESTED MAP ****************************************
private[syntax] trait NestedMapSyntax {

  import cats.implicits._

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}

//************************************* PREDICATE ****************************************
private[syntax] trait PredicateSyntax {
  implicit class PredicateOps[T](p: T => Boolean) {

    /** Combine with another predicate(`T => Boolean`) with `And` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `And` operator.
      */
    def &&(that: T => Boolean): T => Boolean = p.and(that)

    /** Combine with another predicate(`T => Boolean`) with `And` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `And` operator.
      */
    def and(that: T => Boolean): T => Boolean = Predicate.and(p, that)

    /** Combine with another predicate(`T => Boolean`) with `Or` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `Or` operator.
      */
    def ||(that: T => Boolean): T => Boolean = p.or(that)

    /** Combine with another predicate(`T => Boolean`) with `Or` operator.
      *
      * @see [[Predicate]] object for further information.
      * @param that Predicate to combine.
      * @return Result of combination with this instance
      *         with passed predicate instance using `Or` operator.
      */
    def or(that: T => Boolean): T => Boolean = Predicate.or(p, that)
  }
}
