package com.github.geirolz

import com.github.geirolz.advxml.convert._
import com.github.geirolz.advxml.validate.{ValidationInstance, ValidationSyntax}
import com.github.geirolz.advxml.normalize.{XmlNormalizerInstances, XmlNormalizerSyntax}
import com.github.geirolz.advxml.transform.{XmlTransformerInstances, XmlTransformerSyntax}
import com.github.geirolz.advxml.traverse.XmlTraverserSyntax

/**
  * Advxml
  * Created by geirolad on 08/07/2019.
  *
  * @author geirolad
  */
package object advxml {

  object all extends AllSyntax with AllInstances

  object implicits extends AllSyntax {
    object transformer extends XmlTransformerSyntax
    object validation extends ValidationSyntax
    object converter extends ConvertersSyntax
    object traverser extends XmlTraverserSyntax
    object normalizer extends XmlNormalizerSyntax
  }

  object instances extends AllInstances {
    object transformer extends XmlTransformerInstances
    object converter extends ConvertersInstances
    object normalizer extends XmlNormalizerInstances
    object validation extends ValidationInstance
  }
}
