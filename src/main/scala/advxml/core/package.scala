package advxml

import advxml.core.validate.ValidatedNelEx
import cats.Id
import cats.data.Kleisli

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

package object core {

  type Predicate[-A] = A => Boolean
  type XmlPredicate = Predicate[NodeSeq]

  /** Represents a function `A => F[B]` to simplify method and class signatures.
    * This alias represent an error-handled converter to transform `A` into `B` safely.
    * Because the conversion can fail the output is wrapped into `F` in order to handle the errors.
    *
    * @tparam F Output context
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound("Missing implicit PureConverter instance for ${F}, used to covert ${A} to ${B} in ${F}")
  type Converter[F[_], -A, B] = Kleisli[F, A, B]

  /** Represents a function `A => B` to simplify method and class signatures.
    * This alias represent an unsafe converter to transform `A` into `B`.
    *
    * The invocation of this function can fail and/or throw an runtime exception.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound("Missing implicit PureConverter instance, used to covert ${A} to ${B}")
  type PureConverter[-A, B] = Converter[Id, A, B]

  /** Represents a function `A => ValidatedEx[B]` to simplify method and class signatures.
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

  /** Represents a function `O => ValidatedEx[NodeSeq]` to simplify method and class signatures.
    * This function transform a model of type `O` to standard scala-xml library `NodeSeq`, in this case `X`.
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validate.ValidatedNelEx]] in order to handle the errors
    *
    * @see [[core.ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    */
  @implicitNotFound("Missing ModelToXml to transform ${O} into ValidatedEx[${X}]")
  type ModelToXml[-O, X <: NodeSeq] = ValidatedConverter[O, X]

  /** Represents a function `NodeSeq => ValidatedEx[O]` to simplify method and class signatures.
    * This function transform xml model of type `X`, from standard scala-xml library, into a model of type `O`
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validate.ValidatedNelEx]] in order to handle the errors
    *
    * @see [[core.ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    * @tparam O Output model type
    */
  @implicitNotFound("Missing XmlToModel to transform ${X} into ValidatedEx[${O}]")
  type XmlToModel[-X <: NodeSeq, O] = ValidatedConverter[X, O]
}
