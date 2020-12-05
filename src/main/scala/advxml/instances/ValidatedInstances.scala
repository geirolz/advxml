package advxml.instances

import advxml.core.data._
import cats.MonadError
import cats.data.Validated.{Invalid, Valid}

private[instances] trait ValidatedInstances {

  implicit val advxmlValidatedNelExMonadErrorInstanceWithThrowable: MonadError[ValidatedNelEx, Throwable] =
    new MonadError[ValidatedNelEx, Throwable] {

      def raiseError[A](e: Throwable): ValidatedNelEx[A] = Invalid(ThrowableNel.fromThrowable(e))

      def pure[A](x: A): ValidatedNelEx[A] = Valid(x)

      def handleErrorWith[A](fa: ValidatedNelEx[A])(f: Throwable => ValidatedNelEx[A]): ValidatedNelEx[A] =
        fa match {
          case Valid(_)   => fa
          case Invalid(e) => f(ThrowableNel.toThrowable(e))
        }

      def flatMap[A, B](fa: ValidatedNelEx[A])(f: A => ValidatedNelEx[B]): ValidatedNelEx[B] =
        fa match {
          case Valid(a)       => f(a)
          case i @ Invalid(_) => i
        }

      @scala.annotation.tailrec
      def tailRecM[A, B](a: A)(f: A => ValidatedNelEx[Either[A, B]]): ValidatedNelEx[B] =
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
