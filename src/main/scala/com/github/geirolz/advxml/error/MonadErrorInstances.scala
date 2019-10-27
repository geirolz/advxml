package com.github.geirolz.advxml.error

import cats.MonadError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object MonadErrorInstances extends MonadErrorInstances

trait MonadErrorInstances {

  implicit def validationMonadErrorInstance[E]: MonadError[Validated[E, *], E] = new MonadError[Validated[E, *], E] {
    def raiseError[A](e: E): Validated[E, A] = Invalid(e)

    def pure[A](x: A): Validated[E, A] = Valid(x)

    def handleErrorWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] =
      fa match {
        case Valid(a)   => Valid(a)
        case Invalid(e) => f(e)
      }

    def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B]): Validated[E, B] =
      fa match {
        case Valid(a)   => f(a)
        case Invalid(e) => Invalid(e)
      }

    @scala.annotation.tailrec
    def tailRecM[A, B](a: A)(f: A => Validated[E, Either[A, B]]): Validated[E, B] =
      f(a) match {
        case Valid(eitherAb) =>
          eitherAb match {
            case Right(b) => Valid(b)
            case Left(a)  => tailRecM(a)(f)
          }
        case Invalid(e) => Invalid(e)
      }
  }

  implicit def eitherMonadErrorInstance[E]: MonadError[Either[E, *], E] = new MonadError[Either[E, *], E] {

    def raiseError[A](e: E): Either[E, A] = Left(e)

    def pure[A](x: A): Either[E, A] = Right(x)

    def handleErrorWith[A](fa: Either[E, A])(f: E => Either[E, A]): Either[E, A] =
      fa match {
        case Right(a) => Right(a)
        case Left(e)  => f(e)
      }

    def flatMap[A, B](fa: Either[E, A])(f: A => Either[E, B]): Either[E, B] =
      fa match {
        case Right(a) => f(a)
        case Left(e)  => Left(e)
      }

    @scala.annotation.tailrec
    def tailRecM[A, B](a: A)(f: A => Either[E, Either[A, B]]): Either[E, B] =
      f(a) match {
        case Right(eitherAb) =>
          eitherAb match {
            case Right(b) => Right(b)
            case Left(a)  => tailRecM(a)(f)
          }
        case Left(e) => Left(e)
      }
  }
}
