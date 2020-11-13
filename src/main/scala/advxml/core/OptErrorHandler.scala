package advxml.core

import cats.{~>, Alternative}

object OptErrorHandler {

  def apply[F[_]: OptErrorHandler](): OptErrorHandler[F] = implicitly[OptErrorHandler[F]]

  implicit def optErrorHandlerForMonadEx[F[_]: MonadEx]: OptErrorHandler[F] =
    throwable =>
      λ[Option ~> F] {
        case Some(value) => MonadEx[F].pure(value)
        case None        => MonadEx[F].raiseError(throwable)
      }

  implicit def optErrorHandlerForAlternative[F[_]: Alternative]: OptErrorHandler[F] =
    _ =>
      λ[Option ~> F] {
        case Some(value) => Alternative[F].pure(value)
        case None        => Alternative[F].empty
      }
}
