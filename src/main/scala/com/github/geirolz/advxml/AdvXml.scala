package com.github.geirolz.advxml

import com.github.geirolz.advxml.transform.{XmlTransformer, XmlTransformerSyntax}
import com.github.geirolz.advxml.traverse.XmlTraverseSyntax

object AdvXml
  extends XmlTransformer
    with AdvXmlSyntax

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
