package com.github.geirolz.advxml.convert

import cats.data.Validated
import com.github.geirolz.advxml.convert.ValidatedConverter.ValidatedConverter
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

object ValidatedConverter extends ValidatedConverterOps {
  type ValidatedConverter[A, B] = A => ValidatedRes[B]
}

private[convert] trait ValidatedConverterOps {
  def id[A]: ValidatedConverter[A, A] = Validated.Valid[A]
  def apply[A, B](a: A)(implicit convert: ValidatedConverter[A, B]): ValidatedRes[B] = convert(a)
}
