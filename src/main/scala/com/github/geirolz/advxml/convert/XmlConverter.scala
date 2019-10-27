package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}
import com.github.geirolz.advxml.utils.Converter
import com.github.geirolz.advxml.utils.Converter.Converter

import scala.xml.NodeSeq

object XmlConverter {

  type ModelToXml[F[_], -O, X <: NodeSeq] = Converter[F, O, X]
  type XmlToModel[F[_], -X <: NodeSeq, O] = Converter[F, X, O]

  /**
    * Syntactic sugar to convert a [[X]] instance into [[O]] using an implicit [[XmlToModel]] instance.
    * See [[XmlToModel]] for further information.
    *
    * @tparam O Output model type
    * @return [[com.github.geirolz.advxml.error.ValidatedEx]] instance that, if on success case contains [[O]] instance.
    */
  def asXml[F[_], O: ModelToXml[F, ?, X], X <: NodeSeq](model: O): F[X] = Converter(model)

  /**
    * Syntactic sugar to convert a [[O]] instance into [[X]] using an implicit [[ModelToXml]] instance.
    *
    * @tparam X Output model type
    * @return [[com.github.geirolz.advxml.error.ValidatedEx]] instance that, if on success case contains [[X]] instance.
    */
  def asModel[F[_], X <: NodeSeq: XmlToModel[F, ?, O], O](xml: X): F[O] = Converter(xml)
}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[F[_], X <: NodeSeq](xml: X) {

    /**
      * @see [[XmlConverter.asModel()]]
      */
    def as[B](implicit F: XmlToModel[F, X, B]): F[B] = XmlConverter.asModel(xml)
  }

  implicit class ConverterAnyOps[F[_], O](model: O) {

    /**
      * @see [[XmlConverter.asXml()]]
      */
    def asXml[X <: NodeSeq](implicit F: ModelToXml[F, O, X]): F[X] = XmlConverter.asXml(model)
  }
}
