package com.dgz.advxml

import com.dgz.advxml.transform.{XmlTransformer, XmlTransformerSyntax}
import com.dgz.advxml.traverse.XmlTraverseSyntax

object AdvXml
  extends XmlTransformer
    with AdvXmlSyntax

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
