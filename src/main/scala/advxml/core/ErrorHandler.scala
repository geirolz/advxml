package advxml.core

import cats.{~>, Alternative, Eval}
import cats.data.Validated

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound(
  "Cannot find an implicit value for OptErrorHandler of type ${F}. Please try to import advxml._"
)
sealed trait ErrorHandler[F[_], E] extends (Eval[E] => Option ~> F)
sealed trait ExHandler[F[_]] extends ErrorHandler[F, Throwable]

object ErrorHandler extends OptErrorHandlerInstances {

  def fromOption[F[_], E, A](e: => E)(fa: Option[A])(implicit F: ErrorHandler[F, E]): F[A] =
    F(Eval.later(e)).apply(fa)

  def fromTry[F[_], A](fa: Try[A])(implicit eh: ExHandler[F]): F[A] =
    fromOption(fa.failed.get)(fa.toOption)

  def fromEither[F[_], E, A](fa: Either[E, A])(implicit eh: ErrorHandler[F, E]): F[A] =
    fromOption(fa.left.get)(fa.toOption)

  def fromValidated[F[_], E, A](fa: Validated[E, A])(implicit eh: ErrorHandler[F, E]): F[A] =
    fromEither(fa.toEither)

}

private[core] trait OptErrorHandlerInstances {

  implicit def ExHandlerForMonadEx[F[_]: MonadEx]: ExHandler[F] =
    new ExHandler[F] {
      override def apply(throwable: Eval[Throwable]): Option ~> F = λ[Option ~> F] {
        case Some(value) => MonadEx[F].pure(value)
        case None        => MonadEx[F].raiseError(throwable.value)
      }
    }

  implicit def ExHandlerForAlternative[F[_]: Alternative]: ExHandler[F] =
    new ExHandler[F] {
      override def apply(throwable: Eval[Throwable]): Option ~> F = λ[Option ~> F] {
        case Some(value) => Alternative[F].pure(value)
        case None        => Alternative[F].empty
      }
    }
}
