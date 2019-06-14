package com.dg.advxml

import com.dg.advxml.syntax.XmlSyntax
import com.dg.advxml.transform.XmlTransformer

private [advxml] sealed trait AdvXml
  extends XmlTransformer
    with XmlSyntax

object AdvXml extends AdvXml
