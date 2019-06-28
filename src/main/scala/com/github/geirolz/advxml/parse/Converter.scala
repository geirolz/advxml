package com.github.geirolz.advxml.parse

import cats.data.Validated
import com.github.geirolz.advxml.utils.Validation.ValidatedEx

object Converter{

  type Converter[Domain, CoDomain] = Domain => ValidatedEx[CoDomain]

  def id[A]: Converter[A, A] = Validated.Valid[A]

  def apply[A, B](a: A)(implicit convert: Converter[A, B]) : ValidatedEx[B] = convert(a)
}
