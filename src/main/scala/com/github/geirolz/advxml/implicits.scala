package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationSyntax, XmlConverterSyntax, XmlTextSerializerSyntax}
import com.github.geirolz.advxml.transform.XmlTransformerSyntax
import com.github.geirolz.advxml.traverse.XmlTraverseSyntax

object implicits
  extends XmlTransformerSyntax
    with XmlTraverseSyntax
    with XmlConverterSyntax
    with ValidationSyntax
    with XmlTextSerializerSyntax
