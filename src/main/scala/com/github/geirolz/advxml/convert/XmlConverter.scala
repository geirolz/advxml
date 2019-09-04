package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.ValidatedConverter.ValidatedConverter
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}

import scala.xml.NodeSeq

object XmlConverter extends ValidatedConverterOps {
  type ModelToXml[-A, +B <: NodeSeq] = ValidatedConverter[A, B]
  type XmlToModel[-A <: NodeSeq, +B] = ValidatedConverter[A, B]

  def asXml[Obj: ModelToXml[?, Xml], Xml <: NodeSeq](model: Obj): ValidatedRes[Xml] = XmlConverter(model)

  def asModel[Xml <: NodeSeq: XmlToModel[?, Obj], Obj](xml: Xml): ValidatedRes[Obj] = XmlConverter(xml)
}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[Xml <: NodeSeq](xml: Xml) {
    def as[Obj: XmlToModel[Xml, ?]]: ValidatedRes[Obj] = XmlConverter(xml)
  }

  implicit class ConverterAnyOps[Obj](model: Obj) {
    def asXml[Xml <: NodeSeq: ModelToXml[Obj, ?]]: ValidatedRes[Xml] = XmlConverter(model)
  }

}
