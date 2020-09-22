package advxml.core.utils

import advxml.core.validate.MonadEx
import cats.{~>, Alternative}

import scala.annotation.implicitNotFound

//TODO: Common
object OptErrorHandler {

  @implicitNotFound(
    "Cannot find an implicit value for OptErrorHandler of type ${F}. Please try to import advxml._"
  )
  type OptErrorHandler[F[_]] = Throwable => Option ~> F

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
