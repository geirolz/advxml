package com.github.geirolz.advxml.parse

import com.github.geirolz.advxml.parse.Converter.Converter
import com.github.geirolz.advxml.utils.Validation.ValidatedEx

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlParserSyntax {

  implicit class XmlParserOps[Xml <: NodeSeq](xml: Xml){
    def as[Obj](implicit c: Converter[Xml, Obj]) : ValidatedEx[Obj] = Converter(xml)
  }

  implicit class AnyOps[Obj](model: Obj){
    def asXml[Xml <: NodeSeq](implicit c: Converter[Obj, Xml]) : ValidatedEx[Xml] = Converter(model)
  }
}
