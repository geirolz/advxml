package com.github.geirolz.advxml

import cats.MonadError
import cats.data.ValidatedNel

/**
  * Advxml
  * Created by geirolad on 28/10/2019.
  *
  * @author geirolad
  */
package object error {
  type MonadEx[F[_]] = MonadError[F, Throwable]
  type ValidatedEx[+T] = ValidatedNel[Throwable, T]
}
