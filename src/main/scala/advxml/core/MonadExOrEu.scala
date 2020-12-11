package advxml.core

import advxml.core.data.{EitherEx, ValidatedEx}
import cats.Monad

import scala.util.Try

sealed trait MonadExOrEu[F[_]] extends Monad[F] {

  protected val value: Monad[F]

  override def pure[A](x: A): F[A] = value.pure(x)

  override def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] = value.tailRecM(a)(f)

  override def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = value.flatMap(fa)(f)

  def raiseErrorOrEmpty[A](e: => Throwable): F[A] = this match {
    case m @ MonadExCase(_) => m.raiseError(e)
    case m @ MonadEuCase(_) => m.empty
  }
}
case class MonadExCase[F[_]](value: MonadEx[F]) extends MonadExOrEu[F] {
  def raiseError[A](e: => Throwable): F[A] = value.raiseError(e)
}
case class MonadEuCase[F[_]](value: MonadEu[F]) extends MonadExOrEu[F] {
  def empty[A]: F[A] = value.raiseError(())
}

object MonadExOrEu extends MonadExOrAltInstances {

  def apply[F[_]](implicit F: MonadExOrEu[F]): MonadExOrEu[F] = F

  def fromOption[F[_], A](e: => Throwable)(t: Option[A])(implicit F: MonadExOrEu[F]): F[A] =
    t match {
      case None        => F.raiseErrorOrEmpty(e)
      case Some(value) => F.pure(value)
    }

  def fromTry[F[_], A](fa: Try[A])(implicit F: MonadExOrEu[F]): F[A] =
    fromOption(fa.failed.get)(fa.toOption)

  def fromEither[F[_], A](fa: EitherEx[A])(implicit eh: MonadExOrEu[F]): F[A] =
    fromTry(fa.toTry)

  def fromValidated[F[_], A](fa: ValidatedEx[A])(implicit eh: MonadExOrEu[F]): F[A] =
    fromEither(fa.toEither)
}

private[core] sealed trait MonadExOrAltInstances {

  implicit def monadExCaseAsMonadEx[F[_]](implicit M: MonadExCase[F]): MonadEx[F] = M.value

  implicit def monadEuCaseAsMonadEu[F[_]](implicit M: MonadEuCase[F]): MonadEu[F] = M.value

  implicit def monadExAsMonadExOrEu[F[_]](implicit M: MonadEx[F]): MonadExOrEu[F] = MonadExCase(M)

  implicit def monadEuAsMonadExOrEu[F[_]](implicit M: MonadEu[F]): MonadExOrEu[F] = MonadEuCase(M)
}
