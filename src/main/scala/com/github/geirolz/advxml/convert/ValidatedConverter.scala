package com.github.geirolz.advxml.convert

import cats.data.Validated
import com.github.geirolz.advxml.convert.ValidatedConverter.ValidatedConverter
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

object ValidatedConverter extends ValidatedConverterOps {

  /**
    * This type alias define a shortcut to a function from [[A]] to [[ValidatedRes]] of [[B]]
    * This is used to convert an [[A]] instance to [[B]] handling possible errors.
    *
    * @tparam A contravariant input model type
    * @tparam B covariant output model type
    */
  type ValidatedConverter[-A, +B] = A => ValidatedRes[B]
}

private[convert] trait ValidatedConverterOps {

  /**
    * Create an identity [[ValidatedConverter]] so the output will be the
    * input wrapped into [[Validated.Valid]].
    *
    * @tparam A input/output model type
    * @return Input wrapped into [[Validated.Valid]]
    */
  def id[A]: ValidatedConverter[A, A] = Validated.Valid[A]

  /**
    * This method is just a syntactic sugar to convert an [[A]] instance to [[B]]
    * using an implicit [[ValidatedConverter]] instance.
    * See [[ValidatedConverter]] for further information.
    *
    * @param a       Input model instance
    * @param convert [[ValidatedConverter]] instance
    * @tparam A Input model type
    * @tparam B Output model type
    * @return Conversion result.
    *         See [[ValidatedRes]] documentation for further information.
    */
  def apply[A, B](a: A)(implicit convert: ValidatedConverter[A, B]): ValidatedRes[B] = convert(a)
}
