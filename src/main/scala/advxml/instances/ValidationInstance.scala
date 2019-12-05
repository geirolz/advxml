package advxml.instances

import advxml.core.validation.{MonadEx, ThrowableNel, ValidatedEx}
import advxml.core.validation.exceptions.AggregatedException
import cats.data.Validated.{Invalid, Valid}
import cats.MonadError
import cats.data.{NonEmptyList, Validated}

private[instances] trait ValidationInstance {

  implicit val validatedNelMonadErrorThrowableInstance: MonadEx[ValidatedEx] =
    validatedMonadErrorInstance[ThrowableNel, Throwable](
      NonEmptyList.of(_),
      nelE => new AggregatedException(nelE.toList)
    )

  implicit def validatedMonadErrorInstanceSameError[FE, ME](
    implicit C1: ME =:= FE,
    C2: FE =:= ME
  ): MonadError[Validated[FE, *], ME] =
    validatedMonadErrorInstance[FE, ME](C1.apply, C2.apply)

  implicit def validatedMonadErrorInstance[FE, ME](
    implicit toFe: ME => FE,
    toMe: FE => ME
  ): MonadError[Validated[FE, *], ME] =
    new MonadError[Validated[FE, *], ME] {

      def raiseError[A](e: ME): Validated[FE, A] = Invalid(e)

      def pure[A](x: A): Validated[FE, A] = Valid(x)

      def handleErrorWith[A](fa: Validated[FE, A])(f: ME => Validated[FE, A]): Validated[FE, A] =
        fa match {
          case Valid(a)   => Valid(a)
          case Invalid(e) => f(toMe(e))
        }

      def flatMap[A, B](fa: Validated[FE, A])(f: A => Validated[FE, B]): Validated[FE, B] =
        fa match {
          case Valid(a)   => f(a)
          case Invalid(e) => Invalid(e)
        }

      @scala.annotation.tailrec
      def tailRecM[A, B](a: A)(f: A => Validated[FE, Either[A, B]]): Validated[FE, B] =
        f(a) match {
          case Valid(eitherAb) =>
            eitherAb match {
              case Right(b) => Valid(b)
              case Left(a)  => tailRecM(a)(f)
            }
          case Invalid(e) => Invalid(e)
        }
    }
}
