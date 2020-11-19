package advxml.instances

import advxml.core.{MonadEx, OptErrorHandler}
import advxml.core.data.error.AggregatedException
import cats.{~>, Alternative}
import cats.data.NonEmptyList
import cats.kernel.Semigroup

private[advxml] trait AllInstances
    extends AllCommonInstances
    with AllTransforInstances
    with ConverterInstances
    with ValidatedInstances

private[instances] trait AllCommonInstances extends AggregatedExceptionInstances with ErrorHandlerInstances

//********************************* EXCEPTIONS **********************************
private[instances] trait AggregatedExceptionInstances {
  implicit lazy val semigroupInstanceForAggregatedException: Semigroup[Throwable] =
    (x: Throwable, y: Throwable) => new AggregatedException(NonEmptyList.of(x, y))
}

//********************************* ERROR HANDLER **********************************
private[instances] trait ErrorHandlerInstances {

  implicit def optErrorHandlerForMonadEx[F[_]: MonadEx]: OptErrorHandler[F] =
    throwable =>
      λ[Option ~> F] {
        case Some(value) => MonadEx[F].pure(value)
        case None        => MonadEx[F].raiseError(throwable.value)
      }

  implicit def optErrorHandlerForAlternative[F[_]: Alternative]: OptErrorHandler[F] =
    _ =>
      λ[Option ~> F] {
        case Some(value) => Alternative[F].pure(value)
        case None        => Alternative[F].empty
      }
}
