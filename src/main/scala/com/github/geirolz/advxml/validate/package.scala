package com.github.geirolz.advxml

import cats.MonadError
import cats.data.{NonEmptyList, ValidatedNel}

/**
  * Advxml
  * Created by geirolad on 28/10/2019.
  *
  * @author geirolad
  */
package object validate {
  // format: off
  type \/[+A, +B]           = Either[A, B]
  type MonadEx[F[_]]        = MonadError[F, Throwable]
  type MonadNelEx[F[_]]     = MonadError[F, ThrowableNel]
  type EitherEx[+T]         = Either[Throwable, T]
  type EitherNelEx[+T]      = Either[ThrowableNel, T]
  type ValidatedEx[+T]      = ValidatedNel[Throwable, T]
  type ThrowableNel         = NonEmptyList[Throwable]
  // format: on

  object MonadEx {
    def apply[F[_]: MonadEx]: MonadEx[F] = implicitly[MonadEx[F]]
  }
  object MonadNelEx {
    def apply[F[_]: MonadNelEx]: MonadNelEx[F] = implicitly[MonadNelEx[F]]
  }
}
