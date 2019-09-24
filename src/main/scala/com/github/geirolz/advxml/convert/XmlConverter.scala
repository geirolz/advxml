package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.ValidatedConverter.ValidatedConverter
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}

import scala.xml.NodeSeq

object XmlConverter extends ValidatedConverterOps {

  /**
    *
    * @tparam A
    * @tparam B
    */
  type ModelToXml[-A, +B <: NodeSeq] = ValidatedConverter[A, B]

  /**
    *
    * @tparam A
    * @tparam B
    */
  type XmlToModel[-A <: NodeSeq, +B] = ValidatedConverter[A, B]

  /**
    * Syntactic sugar to convert a [[Xml]] instance into [[Obj]] using an implicit [[XmlToModel]] instance.
    * See [[XmlToModel]] for further information.
    *
    * @tparam Obj Output model type
    * @return [[ValidatedRes]] instance that, if on success case contains [[Obj]] instance.
    */
  def asXml[Obj: ModelToXml[?, Xml], Xml <: NodeSeq](model: Obj): ValidatedRes[Xml] = XmlConverter(model)

  /**
    * Syntactic sugar to convert a [[Obj]] instance into [[Xml]] using an implicit [[ModelToXml]] instance.
    *
    * @tparam Xml Output model type
    * @return [[ValidatedRes]] instance that, if on success case contains [[Xml]] instance.
    */
  def asModel[Xml <: NodeSeq: XmlToModel[?, Obj], Obj](xml: Xml): ValidatedRes[Obj] = XmlConverter(xml)
}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[Xml <: NodeSeq](xml: Xml) {

    /**
      * @see [[XmlConverter.asModel()]]
      */
    def as[Obj: XmlToModel[Xml, ?]]: ValidatedRes[Obj] = XmlConverter(xml)
  }

  implicit class ConverterAnyOps[Obj](model: Obj) {

    /**
      * @see [[XmlConverter.asXml()]]
      */
    def asXml[Xml <: NodeSeq: ModelToXml[Obj, ?]]: ValidatedRes[Xml] = XmlConverter(model)
  }

}
