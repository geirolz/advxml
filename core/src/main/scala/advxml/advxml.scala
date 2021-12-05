import advxml.data.ThrowableNel
import cats.ApplicativeError

package object advxml {
  type ApplicativeNelThrow[F[_]] = ApplicativeError[F, ThrowableNel]
  type ApplicativeEu[F[_]]       = ApplicativeError[F, Unit]
}
