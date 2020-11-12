package advxml.instances

import advxml.core.validate.ThrowableNel
import advxml.core.exceptions.AggregatedException
import cats.data.Validated.{Invalid, Valid}
import cats.MonadError
import cats.data.{NonEmptyList, Validated}

private[instances] trait ValidationInstances {

  implicit val throwable_to_ThrowableNel: Throwable => ThrowableNel = {
    case ex: AggregatedException => ex.exceptions
    case ex                      => NonEmptyList.one(ex)
  }

  implicit val throwableNel_to_Throwable: ThrowableNel => Throwable = nelE => new AggregatedException(nelE)

  implicit def validatedMonadErrorInstance[E1, E2](implicit
    toE1: E2 => E1,
    toE2: E1 => E2
  ): MonadError[Validated[E1, *], E2] =
    new MonadError[Validated[E1, *], E2] {

      def raiseError[A](e: E2): Validated[E1, A] = Invalid(toE1(e))

      def pure[A](x: A): Validated[E1, A] = Valid(x)

      def handleErrorWith[A](fa: Validated[E1, A])(f: E2 => Validated[E1, A]): Validated[E1, A] =
        fa match {
          case Valid(_)   => fa
          case Invalid(e) => f(toE2(e))
        }

      def flatMap[A, B](fa: Validated[E1, A])(f: A => Validated[E1, B]): Validated[E1, B] =
        fa match {
          case Valid(a)       => f(a)
          case i @ Invalid(_) => i
        }

      @scala.annotation.tailrec
      def tailRecM[A, B](a: A)(f: A => Validated[E1, Either[A, B]]): Validated[E1, B] =
        f(a) match {
          case Valid(eitherAb) =>
            eitherAb match {
              case Right(b) => Valid(b)
              case Left(a)  => tailRecM(a)(f)
            }
          case i @ Invalid(_) => i
        }
    }
}
