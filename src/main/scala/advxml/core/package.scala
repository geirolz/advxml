package advxml

import cats.MonadError

package object core {
  type MonadEx[F[_]] = MonadError[F, Throwable]
  type MonadEu[F[_]] = MonadError[F, Unit]
}
