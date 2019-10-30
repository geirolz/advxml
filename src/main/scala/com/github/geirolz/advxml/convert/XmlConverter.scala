package com.github.geirolz.advxml.convert

import cats.data.Validated.Valid
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}
import com.github.geirolz.advxml.error.ValidatedEx
import com.github.geirolz.advxml.utils.Converter
import com.github.geirolz.advxml.utils.Converter.Converter

import scala.xml.NodeSeq

object XmlConverter {

  type XmlConverter[-A, B] = Converter[ValidatedEx, A, B]
  type ModelToXml[-O, X <: NodeSeq] = XmlConverter[O, X]
  type XmlToModel[-X <: NodeSeq, O] = XmlConverter[X, O]

  /**
    * Create an always valid converter that return the input instance.
    * @tparam A Endo converter input and output type
    * @return Identity converter instance
    */
  def id[A]: XmlConverter[A, A] = Valid(_)

  /**
    *  Create an always valid converter that return the passed value ignoring the converter input.
    * @param v Value returned when the [[XmlConverter]] is invoked, the converter input is ignored.
    * @tparam B Convert output type
    * @return Constant converter instance
    */
  def const[A, B](v: B): XmlConverter[A, B] = _ => Valid(v)

  /**
    * Syntactic sugar to convert a [[X]] instance into [[O]] using an implicit [[XmlToModel]] instance.
    * See [[XmlToModel]] for further information.
    *
    * @tparam O Object input model type
    * @tparam X Output xml type
    * @return [[com.github.geirolz.advxml.error.ValidatedEx]] instance that, if on success case contains [[O]] instance.
    */
  def asXml[O: ModelToXml[*, X], X <: NodeSeq](model: O): ValidatedEx[X] = Converter(model)

  /**
    * Syntactic sugar to convert a [[O]] instance into [[X]] using an implicit [[ModelToXml]] instance.

    * @tparam X Input xml model type
    * @tparam O Object output model type
    * @return [[com.github.geirolz.advxml.error.ValidatedEx]] instance that, if on success case contains [[X]] instance.
    */
  def asModel[X <: NodeSeq: XmlToModel[*, O], O](xml: X): ValidatedEx[O] = Converter(xml)
}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[F[_], X <: NodeSeq](xml: X) {

    /**
      * @see [[XmlConverter.asModel()]]
      */
    def as[B](implicit F: XmlToModel[X, B]): ValidatedEx[B] = XmlConverter.asModel(xml)
  }

  implicit class ConverterAnyOps[F[_], O](model: O) {

    /**
      * @see [[XmlConverter.asXml()]]
      */
    def asXml[X <: NodeSeq](implicit F: ModelToXml[O, X]): ValidatedEx[X] = XmlConverter.asXml(model)
  }
}
