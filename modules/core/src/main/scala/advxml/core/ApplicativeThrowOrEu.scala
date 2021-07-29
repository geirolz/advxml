package advxml.core

import advxml.core.data._
import cats.{Applicative, ApplicativeThrow}

import scala.annotation.{implicitAmbiguous, implicitNotFound}
import scala.util.Try

@implicitAmbiguous(
  """Multiple instances of ApplicativeError[${F}, Throwable] or ApplicativeError[${F}, Unit] in the scope," +
  ", try to set {F} type explicitly."""
)
@implicitNotFound("""Missing ApplicativeError[${F}, Throwable] or ApplicativeError[${F}, Unit] in the scope.""")
sealed trait ApplicativeThrowOrEu[F[_]] extends Applicative[F] {

  val app: Applicative[F]

  override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = app.ap(ff)(fa)

  override def pure[A](x: A): F[A] = app.pure(x)

  def raiseErrorOrEmpty[A](e: => Throwable): F[A] = this match {
    case m @ ApplicativeThrowCase(_)    => m.raiseError(e)
    case m @ ApplicativeNelThrowCase(_) => m.raiseError(e)
    case m @ ApplicativeEuCase(_)       => m.empty
  }
}
case class ApplicativeThrowCase[F[_]](app: ApplicativeThrow[F]) extends ApplicativeThrowOrEu[F] {
  def raiseError[A](e: => Throwable): F[A] = app.raiseError(e)
}
case class ApplicativeNelThrowCase[F[_]](app: ApplicativeNelThrow[F]) extends ApplicativeThrowOrEu[F] {
  def raiseError[A](e: => Throwable): F[A] = app.raiseError(ThrowableNel.fromThrowable(e))
}
case class ApplicativeEuCase[F[_]](app: ApplicativeEu[F]) extends ApplicativeThrowOrEu[F] {
  def empty[A]: F[A] = app.raiseError(())
}

object ApplicativeThrowOrEu extends AppExOrEuInstances {

  def apply[F[_]](implicit F: ApplicativeThrowOrEu[F]): F.type = F

  def fromOption[F[_]: ApplicativeThrowOrEu, A](e: => Throwable)(t: Option[A]): F[A] =
    t match {
      case None        => ApplicativeThrowOrEu[F].raiseErrorOrEmpty(e)
      case Some(value) => ApplicativeThrowOrEu[F].pure(value)
    }

  def fromTry[F[_]: ApplicativeThrowOrEu, A](fa: Try[A]): F[A] =
    fromOption[F, A](fa.failed.get)(fa.toOption)

  def fromEitherThrow[F[_]: ApplicativeThrowOrEu, A](fa: EitherThrow[A]): F[A] =
    fromTry[F, A](fa.toTry)

  def fromEitherNelThrow[F[_]: ApplicativeThrowOrEu, A](fa: EitherNelThrow[A]): F[A] =
    fromEitherThrow[F, A](fa.swap.map(ThrowableNel.toThrowable).swap)

  def fromValidatedEx[F[_]: ApplicativeThrowOrEu, A](fa: ValidatedEx[A]): F[A] =
    fromEitherThrow[F, A](fa.toEither)

  def fromValidatedNelEx[F[_]: ApplicativeThrowOrEu, A](fa: ValidatedNelEx[A]): F[A] =
    fromEitherNelThrow[F, A](fa.toEither)
}

private[core] sealed trait AppExOrEuInstances {

  implicit def appExAsAppExOrEu[F[_]](implicit M: ApplicativeThrow[F]): ApplicativeThrowCase[F] =
    ApplicativeThrowCase(M)

  implicit def appNelExAsAppExOrEu[F[_]](implicit M: ApplicativeNelThrow[F]): ApplicativeNelThrowCase[F] =
    ApplicativeNelThrowCase(M)

  implicit def appEuAsAppExOrEu[F[_]](implicit M: ApplicativeEu[F]): ApplicativeEuCase[F] =
    ApplicativeEuCase(M)
}
