package advxml.core.convert

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

package object xml {

  /**
    * Represents a function `O => ValidatedEx[NodeSeq]` to simplify method and class signatures.
    * This function transform a model of type `O` to standard scala-xml library `NodeSeq`, in this case `X`.
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validate.ValidatedNelEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    */
  @implicitNotFound("Missing ModelToXml to transform ${O} into ValidatedEx[${X}]")
  type ModelToXml[-O, X <: NodeSeq] = ValidatedConverter[O, X]

  /**
    * Represents a function `NodeSeq => ValidatedEx[O]` to simplify method and class signatures.
    * This function transform xml model of type `X`, from standard scala-xml library, into a model of type `O`
    * Because the conversion can fail the output is wrapped into cats [[advxml.core.validate.ValidatedNelEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model, type constraint ensures that `X` is a subtype of scala-xml `NodeSeq`
    * @tparam O Output model type
    */
  @implicitNotFound("Missing XmlToModel to transform ${X} into ValidatedEx[${O}]")
  type XmlToModel[-X <: NodeSeq, O] = ValidatedConverter[X, O]
}
