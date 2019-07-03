package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationSyntax, XmlConverterSyntax, XmlTextSerializerInstances, XmlTextSerializerSyntax}
import com.github.geirolz.advxml.transform.{XmlTransformerActions, XmlTransformerSyntax}
import com.github.geirolz.advxml.traverse.XmlTraverseSyntax

object AdvXml extends XmlTransformerActions
  with AdvXmlInstances
  with AdvXmlSyntax

private[advxml] sealed trait AdvXmlInstances
  extends XmlTextSerializerInstances

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
    with XmlConverterSyntax
    with ValidationSyntax
    with XmlTextSerializerSyntax
