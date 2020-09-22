package advxml.core

import advxml.core.validate.ValidatedNelEx
import cats.Id
import cats.data.Kleisli

import scala.annotation.implicitNotFound

package object convert {

  /**
    * Represents a function `A => F[B]` to simplify method and class signatures.
    * This alias represent an error-handled converter to transform `A` into `B` safely.
    * Because the conversion can fail the output is wrapped into `F` in order to handle the errors.
    *
    * @tparam F Output context
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound("Missing implicit PureConverter instance for ${F}, used to covert ${A} to ${B} in ${F}")
  type Converter[F[_], -A, B] = Kleisli[F, A, B]

  /**
    * Represents a function `A => B` to simplify method and class signatures.
    * This alias represent an unsafe converter to transform `A` into `B`.
    *
    * The invocation of this function can fail and/or throw an runtime exception.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound("Missing implicit PureConverter instance, used to covert ${A} to ${B}")
  type PureConverter[-A, B] = Converter[Id, A, B]

  /**
    * Represents a function `A => ValidatedEx[B]` to simplify method and class signatures.
    * Converter to easily transform an object of type `A` to another object of type `B`.
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validate.ValidatedNelEx]] in order to handle the errors.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound(
    "Missing implicit ValidatedConverter instance for ValidatedNelEx, used to covert ${A} to ${B} in ValidatedNelEx"
  )
  type ValidatedConverter[-A, B] = Converter[ValidatedNelEx, A, B]
}
