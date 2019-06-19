package com.dg.advxml

import com.dg.advxml.traverse.XmlTraverseSyntax
import com.dg.advxml.transform.{XmlTransformer, XmlTransformerSyntax}

object AdvXml
  extends XmlTransformer
    with AdvXmlSyntax

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
