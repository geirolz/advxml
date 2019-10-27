package com.github.geirolz.advxml.utils

object Converter {
  type Converter[F[_], -A, B] = A => F[B]

  def apply[F[_], A, B](a: A)(implicit F: Converter[F, A, B]): F[B] = F.apply(a)
}
