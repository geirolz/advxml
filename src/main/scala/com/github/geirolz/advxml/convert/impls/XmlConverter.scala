package com.github.geirolz.advxml.convert.impls

import com.github.geirolz.advxml.convert.impls.ValidatedConverter.ValidatedConverter
import com.github.geirolz.advxml.validate.ValidatedEx

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

object XmlConverter {

  /**
    * Represents a function `O => ValidatedEx[NodeSeq]` to simplify method and class signatures.
    * This function transform a model of type [[O]] to standard scala-xml library [[NodeSeq]], in this case [[X]].
    * Because the conversion can fail the output is wrapped into cats [[ValidatedEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type, type constraint ensures that [[X]] is a subtype of scala-xml [[NodeSeq]]
    */
  type ModelToXml[-O, X <: NodeSeq] = ValidatedConverter[O, X]

  /**
    * Represents a function `NodeSeq => ValidatedEx[O]` to simplify method and class signatures.
    * This function transform xml model of type [[X]], from standard scala-xml library, into a model of type [[O]]
    * Because the conversion can fail the output is wrapped into cats [[ValidatedEx]] in order to handle the errors
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model, type constraint ensures that [[X]] is a subtype of scala-xml [[NodeSeq]]
    * @tparam O Output model type
    */
  type XmlToModel[-X <: NodeSeq, O] = ValidatedConverter[X, O]

  /**
    * Syntactic sugar to convert a [[O]] instance into [[X]] using an implicit [[ModelToXml]] instance.
    * This method catch a [[XmlToModel]] instance in the scope that conforms with types [[X]][[O]] and then invoke
    * in it the method `apply` passing `xml`.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam X Contravariant input xml model type
    * @tparam O Object output type
    * @return [[ValidatedEx]] instance that, if on success case contains [[X]] instance.
    */
  @implicitNotFound("Missing XmlToModel to transform ${X} into ValidatedEx[${O}]")
  def asModel[X <: NodeSeq: XmlToModel[*, O], O](xml: X): ValidatedEx[O] = Converter(xml)

  /**
    * Syntactic sugar to convert a [[X]] instance into [[O]] using an implicit [[XmlToModel]] instance.
    *
    * This method catch a [[ModelToXml]] instance in the scope that conforms with types [[O]][[X]] and then invoke
    * in it the method `apply` passing `model`.
    *
    * See [[ModelToXml]] for further information.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Contravariant input model type
    * @tparam X Output xml type
    * @return [[ValidatedEx]] instance that, if on success case contains [[O]] instance.
    */
  @implicitNotFound("Missing ModelToXml to transform ${O} into ValidatedEx[${X}]")
  def asXml[O: ModelToXml[*, X], X <: NodeSeq](model: O): ValidatedEx[X] = Converter(model)
}
