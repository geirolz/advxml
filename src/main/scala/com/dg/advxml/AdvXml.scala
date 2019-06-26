package com.dg.advxml

import com.dg.advxml.transform.{XmlTransformer, XmlTransformerSyntax}
import com.dg.advxml.traverse.XmlTraverseSyntax

object AdvXml
  extends XmlTransformer
    with AdvXmlSyntax

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
