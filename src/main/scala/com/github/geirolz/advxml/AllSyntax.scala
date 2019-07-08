package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationSyntax, XmlConverterSyntax, XmlTextSerializerSyntax}
import com.github.geirolz.advxml.transform.XmlTransformerSyntax
import com.github.geirolz.advxml.traverse.XmlTraverserSyntax

private [advxml] trait AllSyntax
  extends XmlTransformerSyntax
    with XmlTraverserSyntax
    with XmlConverterSyntax
    with ValidationSyntax
    with XmlTextSerializerSyntax