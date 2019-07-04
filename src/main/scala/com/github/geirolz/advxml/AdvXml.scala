package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert._
import com.github.geirolz.advxml.transform.{XmlTransformerActions, XmlTransformerSyntax}
import com.github.geirolz.advxml.traverse.XmlTraverseSyntax

object AdvXml extends XmlTransformerActions
  with AdvXmlInstances
  with AdvXmlSyntax

private[advxml] sealed trait AdvXmlInstances
  extends XmlTextSerializerInstances
  with ValidationInstances

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
    with XmlConverterSyntax
    with ValidationSyntax
    with XmlTextSerializerSyntax
