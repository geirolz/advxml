package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.ConvertersSyntax
import com.github.geirolz.advxml.validate.ValidationSyntax
import com.github.geirolz.advxml.normalize.XmlNormalizerSyntax
import com.github.geirolz.advxml.predicate.PredicateSyntax
import com.github.geirolz.advxml.transform.XmlTransformerSyntax
import com.github.geirolz.advxml.traverse.XmlTraverserAbstractSyntax

private[advxml] trait AllSyntax
    extends XmlTransformerSyntax
    with XmlTraverserAbstractSyntax
    with XmlNormalizerSyntax
    with ConvertersSyntax
    with ValidationSyntax
    with PredicateSyntax
