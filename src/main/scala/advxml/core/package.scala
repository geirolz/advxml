package advxml

import cats.MonadError

package object core {
  type MonadEx[F[_]] = MonadError[F, Throwable]
  type MonadPlus[F[_]] = MonadError[F, Unit]
}
