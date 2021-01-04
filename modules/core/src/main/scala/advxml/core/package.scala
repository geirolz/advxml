package advxml

import advxml.core.data.ThrowableNel
import cats.{ApplicativeError, MonadError}

package object core {
  type MonadEx[F[_]] = MonadError[F, Throwable]

  type AppEx[F[_]] = ApplicativeError[F, Throwable]
  type AppNelEx[F[_]] = ApplicativeError[F, ThrowableNel]
  type AppEu[F[_]] = ApplicativeError[F, Unit]
}
