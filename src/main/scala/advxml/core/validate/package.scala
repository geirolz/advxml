package advxml.core

import cats.data.{EitherNel, NonEmptyList, ValidatedNel}
import cats.MonadError

package object validate {
  // format: off
  type \/[+A, +B]           = Either[A, B]
  type MonadEx[F[_]]        = MonadError[F, Throwable]
  type MonadNelEx[F[_]]     = MonadError[F, ThrowableNel]
  type EitherEx[+T]         = Either[Throwable, T]
  type EitherNelEx[+T]      = EitherNel[Throwable, T]
  type ValidatedEx[+T]      = ValidatedNel[Throwable, T]
  type ThrowableNel         = NonEmptyList[Throwable]
  // format: on

  object MonadEx {
    def apply[F[_]: MonadEx]: MonadEx[F] = implicitly[MonadEx[F]]
  }
  object MonadNelEx {
    def apply[F[_]: MonadNelEx]: MonadNelEx[F] = implicitly[MonadNelEx[F]]
  }
}
