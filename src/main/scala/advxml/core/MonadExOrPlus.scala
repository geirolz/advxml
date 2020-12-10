package advxml.core

import cats.{Monad, MonadError}
import cats.data.Validated

import scala.util.Try

sealed trait MonadExOrPlus[F[_]] extends Monad[F] {

  protected val value: Monad[F]

  override def pure[A](x: A): F[A] = value.pure(x)

  override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = value.tailRecM(a)(f)

  override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = value.flatMap(fa)(f)

  def raiseErrorOrEmpty[A](e: => Throwable): F[A] = this match {
    case MonadExCase(value)   => value.raiseError(e)
    case MonadPlusCase(value) => value.raiseError(())
  }
}
case class MonadExCase[F[_]](value: MonadEx[F]) extends MonadExOrPlus[F]
case class MonadPlusCase[F[_]](value: MonadError[F, Unit]) extends MonadExOrPlus[F]

object MonadExOrPlus extends MonadExOrAltInstances {

  def apply[F[_]](implicit F: MonadExOrPlus[F]): MonadExOrPlus[F] = F

  def fromOption[F[_], A](e: => Throwable)(t: Option[A])(implicit F: MonadExOrPlus[F]): F[A] = {
    t match {
      case None        => F.raiseErrorOrEmpty(e)
      case Some(value) => F.pure(value)
    }
  }

  def fromTry[F[_], A](fa: Try[A])(implicit F: MonadExOrPlus[F]): F[A] =
    fromOption(fa.failed.get)(fa.toOption)

  def fromEither[F[_], A](fa: Either[Throwable, A])(implicit eh: MonadExOrPlus[F]): F[A] =
    fromOption(fa.swap.getOrElse(throw new RuntimeException("Missing Error")))(fa.toOption)

  def fromValidated[F[_], A](fa: Validated[Throwable, A])(implicit eh: MonadExOrPlus[F]): F[A] =
    fromEither(fa.toEither)
}

private[core] sealed trait MonadExOrAltInstances {

  implicit def monadExCaseAsMonadEx[F[_]](implicit M: MonadExCase[F]): MonadEx[F] = M.value

  implicit def MonadExOrAltAsAlternative[F[_]](implicit M: MonadPlusCase[F]): MonadPlus[F] = M.value

  implicit def monadExAsMonadExOrAlt[F[_]](implicit M: MonadEx[F]): MonadExOrPlus[F] = MonadExCase(M)

  implicit def alternativeAsMonadExOrAlt[F[_]](implicit M: MonadPlus[F]): MonadExOrPlus[F] = MonadPlusCase(M)
}
