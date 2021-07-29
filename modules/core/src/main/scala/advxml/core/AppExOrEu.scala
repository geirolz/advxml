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
sealed trait AppExOrEu[F[_]] extends Applicative[F] {

  val app: Applicative[F]

  override def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = app.ap(ff)(fa)

  override def pure[A](x: A): F[A] = app.pure(x)

  def raiseErrorOrEmpty[A](e: => Throwable): F[A] = this match {
    case m @ AppExCase(_)    => m.raiseError(e)
    case m @ AppNelExCase(_) => m.raiseError(e)
    case m @ AppEuCase(_)    => m.empty
  }
}
case class AppExCase[F[_]](app: ApplicativeThrow[F]) extends AppExOrEu[F] {
  def raiseError[A](e: => Throwable): F[A] = app.raiseError(e)
}
case class AppNelExCase[F[_]](app: ApplicativeNelThrow[F]) extends AppExOrEu[F] {
  def raiseError[A](e: => Throwable): F[A] = app.raiseError(ThrowableNel.fromThrowable(e))
}
case class AppEuCase[F[_]](app: ApplicativeEu[F]) extends AppExOrEu[F] {
  def empty[A]: F[A] = app.raiseError(())
}

object AppExOrEu extends AppExOrEuInstances {

  def apply[F[_]](implicit F: AppExOrEu[F]): F.type = F

  def fromOption[F[_]: AppExOrEu, A](e: => Throwable)(t: Option[A]): F[A] =
    t match {
      case None        => AppExOrEu[F].raiseErrorOrEmpty(e)
      case Some(value) => AppExOrEu[F].pure(value)
    }

  def fromTry[F[_]: AppExOrEu, A](fa: Try[A]): F[A] =
    fromOption[F, A](fa.failed.get)(fa.toOption)

  def fromEitherEx[F[_]: AppExOrEu, A](fa: EitherEx[A]): F[A] =
    fromTry[F, A](fa.toTry)

  def fromEitherNelEx[F[_]: AppExOrEu, A](fa: EitherNelEx[A]): F[A] =
    fromEitherEx[F, A](fa.swap.map(ThrowableNel.toThrowable).swap)

  def fromValidatedEx[F[_]: AppExOrEu, A](fa: ValidatedEx[A]): F[A] =
    fromEitherEx[F, A](fa.toEither)

  def fromValidatedNelEx[F[_]: AppExOrEu, A](fa: ValidatedNelEx[A]): F[A] =
    fromEitherNelEx[F, A](fa.toEither)
}

private[core] sealed trait AppExOrEuInstances {

  implicit def appExAsAppExOrEu[F[_]](implicit M: ApplicativeThrow[F]): AppExCase[F] = AppExCase(M)

  implicit def appNelExAsAppExOrEu[F[_]](implicit M: ApplicativeNelThrow[F]): AppNelExCase[F] = AppNelExCase(M)

  implicit def appEuAsAppExOrEu[F[_]](implicit M: ApplicativeEu[F]): AppEuCase[F] = AppEuCase(M)
}
