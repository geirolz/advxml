package advxml.core.convert.xml

import advxml.core.convert
import advxml.core.convert.ValidatedConverter
import advxml.core.validate.ValidatedEx

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

object XmlConverter {

  /**
    * Syntactic sugar to convert a `O` instance into `X` using an implicit [[ModelToXml]] instance.
    * This method catch a [[XmlToModel]] instance in the scope that conforms with types `X``O` and then invoke
    * in it the method `apply` passing `xml`.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model type
    * @tparam O Object output type
    * @return [[advxml.core.validate.ValidatedEx]] instance that, if on success case contains `X` instance.
    */
  @implicitNotFound("Missing XmlToModel to transform ${X} into ValidatedEx[${O}]")
  def asModel[X <: NodeSeq: XmlToModel[*, O], O](xml: X): ValidatedEx[O] = ValidatedConverter[X, O].run(xml)

  /**
    * Syntactic sugar to convert a `X` instance into `O` using an implicit [[XmlToModel]] instance.
    *
    * This method catch a [[ModelToXml]] instance in the scope that conforms with types `O``X` and then invoke
    * in it the method `apply` passing `model`.
    *
    * See [[ModelToXml]] for further information.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type
    * @return [[advxml.core.validate.ValidatedEx]] instance that, if on success case contains `O` instance.
    */
  @implicitNotFound("Missing ModelToXml to transform ${O} into ValidatedEx[${X}]")
  def asXml[O: convert.xml.ModelToXml[*, X], X <: NodeSeq](model: O): ValidatedEx[X] =
    ValidatedConverter[O, X].run(model)
}
