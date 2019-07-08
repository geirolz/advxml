package com.github.geirolz.advxml.convert

import cats.data.Validated
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}

import scala.xml.NodeSeq

object XmlConverter {

  type ValidatedConverter[A, B] = A => ValidatedRes[B]
  type ModelToXml[A, B <: NodeSeq] = ValidatedConverter[A, B]
  type XmlToModel[A <: NodeSeq, B] = ValidatedConverter[A, B]

  def id[A]: ValidatedConverter[A, A] = Validated.Valid[A]

  def apply[A, B](a: A)(implicit convert: ValidatedConverter[A, B]): ValidatedRes[B] = convert(a)


  def asXml[Obj, Xml <: NodeSeq](model: Obj)(implicit c: ModelToXml[Obj, Xml]): ValidatedRes[Xml] = apply(model)

  def asModel[Xml <: NodeSeq, Obj](xml: Xml)(implicit c: XmlToModel[Xml, Obj]): ValidatedRes[Obj] = apply(xml)

}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[Xml <: NodeSeq](xml: Xml) {
    def as[Obj](implicit c: XmlToModel[Xml, Obj]): ValidatedRes[Obj] = XmlConverter(xml)
  }

  implicit class ConverterAnyOps[Obj](model: Obj) {
    def asXml[Xml <: NodeSeq](implicit c: ModelToXml[Obj, Xml]): ValidatedRes[Xml] = XmlConverter(model)
  }

}
