package advxml.core

import cats.Eval

object OptErrorHandler {

  def apply[F[_], A](e: => Throwable)(fa: Option[A])(implicit F: OptErrorHandler[F]): F[A] =
    F(Eval.later(e)).apply(fa)
}
