package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationSyntax, XmlConverterSyntax, XmlTextSerializerSyntax}
import com.github.geirolz.advxml.transform.XmlTransformerSyntax
import com.github.geirolz.advxml.traverse.XmlTraverserSyntax

object implicits
  extends XmlTransformerSyntax
    with XmlTraverserSyntax
    with XmlConverterSyntax
    with ValidationSyntax
    with XmlTextSerializerSyntax {

  object transformer extends XmlTransformerSyntax
  object validation extends ValidationSyntax
  object textSerializer extends XmlTextSerializerSyntax
  object converter extends XmlConverterSyntax
  object traverser extends XmlTraverserSyntax
}
