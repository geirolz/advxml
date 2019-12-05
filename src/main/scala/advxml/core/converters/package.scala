package advxml.core

import advxml.core.validation.ValidatedEx
import cats.Id
import cats.data.Kleisli

import scala.xml.{NodeSeq, Text}

package object converters {

  /**
    * Represents a function `A => F[B]` to simplify method and class signatures.
    * This alias represent an error-handled converter to transform `A` into `B` safely.
    * Because the conversion can fail the output is wrapped into `F` in order to handle the errors.
    *
    * @tparam F Output context
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
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
  type UnsafeConverter[-A, B] = Converter[Id, A, B]

  /**
    * Represents a function `A => ValidatedEx[B]` to simplify method and class signatures.
    * Converter to easily transform an object of type `A` to another object of type `B`.
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validation.ValidatedEx]] in order to handle the errors.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  type ValidatedConverter[-A, B] = Converter[ValidatedEx, A, B]

  /**
    * Represents a function `A => Text` to simplify method and class signatures.
    * This alias represent an unsafe converter to transform `A` into `Text`.
    *
    * The invocation of this function can fail and/or throw an runtime exception.
    *
    * @see [[advxml.core.converters.UnsafeConverter]] for further information.
    * @tparam A Contravariant input object type
    */
  type TextConverter[-A] = UnsafeConverter[A, Text]

  /**
    * Represents a function `O => ValidatedEx[NodeSeq]` to simplify method and class signatures.
    * This function transform a model of type `O` to standard scala-xml library `NodeSeq`, in this case `X`.
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validation.ValidatedEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    */
  type ModelToXml[-O, X <: NodeSeq] = ValidatedConverter[O, X]

  /**
    * Represents a function `NodeSeq => ValidatedEx[O]` to simplify method and class signatures.
    * This function transform xml model of type `X`, from standard scala-xml library, into a model of type `O`
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validation.ValidatedEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    * @tparam O Output model type
    */
  type XmlToModel[-X <: NodeSeq, O] = ValidatedConverter[X, O]
}
