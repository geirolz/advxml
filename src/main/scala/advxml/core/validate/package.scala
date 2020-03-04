package advxml.core

import cats.data.{EitherNel, NonEmptyList, ValidatedNel}
import cats.MonadError

package object validate {
  // format: off
  @deprecated(message = "This type alias will be removed in the future releases, please use ValidatedNelEx instead.", since = "2.2.0")
  type ValidatedEx[+T]      = ValidatedNelEx[T]
  type MonadEx[F[_]]        = MonadError[F, Throwable]
  type MonadNelEx[F[_]]     = MonadError[F, ThrowableNel]
  type EitherEx[+T]         = Either[Throwable, T]
  type EitherNelEx[+T]      = EitherNel[Throwable, T]
  type ValidatedNelEx[+T]   = ValidatedNel[Throwable, T]
  type ThrowableNel         = NonEmptyList[Throwable]
  // format: on

  object MonadEx {
    def apply[F[_]: MonadEx]: MonadEx[F] = implicitly[MonadEx[F]]
  }
  object MonadNelEx {
    def apply[F[_]: MonadNelEx]: MonadNelEx[F] = implicitly[MonadNelEx[F]]
  }
}
