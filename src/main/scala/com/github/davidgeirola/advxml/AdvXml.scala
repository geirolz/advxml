package com.github.davidgeirola.advxml

import com.github.davidgeirola.advxml.transform.{XmlTransformer, XmlTransformerSyntax}
import com.github.davidgeirola.advxml.traverse.XmlTraverseSyntax

object AdvXml
  extends XmlTransformer
    with AdvXmlSyntax

private [advxml] sealed trait AdvXmlSyntax
  extends XmlTransformerSyntax
  with XmlTraverseSyntax
