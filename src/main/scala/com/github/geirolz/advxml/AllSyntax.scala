package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.ConvertersSyntax
import com.github.geirolz.advxml.validate.ValidationSyntax
import com.github.geirolz.advxml.normalize.XmlNormalizerSyntax
import com.github.geirolz.advxml.transform.XmlTransformerSyntax
import com.github.geirolz.advxml.traverse.XmlTraverserSyntax

private[advxml] trait AllSyntax
    extends XmlTransformerSyntax
    with XmlTraverserSyntax
    with XmlNormalizerSyntax
    with ConvertersSyntax
    with ValidationSyntax
