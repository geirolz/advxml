package com.github.geirolz.advxml.parse

import com.github.geirolz.advxml.parse.Converter.Converter
import com.github.geirolz.advxml.utils.Validation.ValidatedEx

import scala.xml.NodeSeq


object XmlParser {

  def asXml[Obj, Xml <: NodeSeq](model: Obj)(implicit c: Converter[Obj, Xml]) : ValidatedEx[Xml] = Converter(model)

  def as[Xml <: NodeSeq, Obj](xml: Xml)(implicit c: Converter[Xml, Obj]) : ValidatedEx[Obj] = Converter(xml)


  object ops extends XmlParserSyntax
}
