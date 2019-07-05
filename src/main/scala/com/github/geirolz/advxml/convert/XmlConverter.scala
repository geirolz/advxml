package com.github.geirolz.advxml.convert

import cats.data.Validated
import com.github.geirolz.advxml.convert.Validation.ValidationRes
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}

import scala.xml.NodeSeq

object XmlConverter {

  type ValidatedConverter[A, B] = A => ValidationRes[B]
  type ModelToXml[A, B <: NodeSeq] = ValidatedConverter[A, B]
  type XmlToModel[A <: NodeSeq, B] = ValidatedConverter[A, B]

  def id[A]: ValidatedConverter[A, A] = Validated.Valid[A]

  def apply[A, B](a: A)(implicit convert: ValidatedConverter[A, B]): ValidationRes[B] = convert(a)


  def asXml[Obj, Xml <: NodeSeq](model: Obj)(implicit c: ModelToXml[Obj, Xml]): ValidationRes[Xml] = apply(model)

  def asModel[Xml <: NodeSeq, Obj](xml: Xml)(implicit c: XmlToModel[Xml, Obj]): ValidationRes[Obj] = apply(xml)


  object implicits extends XmlConverterSyntax

}

private[advxml] trait XmlConverterSyntax {

  implicit class XmlConverterOps[Xml <: NodeSeq](xml: Xml) {
    def as[Obj](implicit c: XmlToModel[Xml, Obj]): ValidationRes[Obj] = XmlConverter(xml)
  }

  implicit class ConverterAnyOps[Obj](model: Obj) {
    def asXml[Xml <: NodeSeq](implicit c: ModelToXml[Obj, Xml]): ValidationRes[Xml] = XmlConverter(model)
  }

}
