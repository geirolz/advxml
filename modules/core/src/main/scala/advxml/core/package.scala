package advxml

import advxml.core.data.ThrowableNel
import cats.ApplicativeError

package object core {
  type ApplicativeNelThrow[F[_]] = ApplicativeError[F, ThrowableNel]
  type ApplicativeEu[F[_]]       = ApplicativeError[F, Unit]
}
