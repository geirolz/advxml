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

  /*
   * In order to keep project clean keep in mind the following rules:
   * - Each object represent a feature
   * - Each feature must provide a trait containing all syntax implicits named `[feature_name]Syntax`
   * - For each object must be exist a package with the same name under `com.github.geirolz.advxml`
   */
  /**
    * This object is the entry point to access to all syntax implicits provided by Advxml.
    *
    * You can import all implicits using:
    * {{{
    *   import com.github.geirolz.advxml.implicits._
    * }}}
    *
    * Otherwise you can import only a specific part of implicits using:
    * {{{
    *   //import com.github.geirolz.advxml.implicits.[feature_name]._
    *   //example
    *   import com.github.geirolz.advxml.implicits.transform._
    * }}}
    */
  object implicits extends AllSyntax {
    // format: off
    object transform  extends XmlTransformerSyntax
    object convert    extends ConvertersSyntax
    object normalize  extends XmlNormalizerSyntax
    object validate   extends ValidationSyntax
    object traverse   extends XmlTraverserSyntax
    // format: on
  }

  /*
   * In order to keep project clean keep in mind the following rules:
   * - Each object represent a feature
   * - Each feature must provide a trait containing all feature instances named `[feature_name]Instances`
   * - For each object must be exist a package with the same name under `com.github.geirolz.advxml`
   */
  /**
    * This object is the entry point to access to all features instances provided by Advxml.
    *
    * You can import all instances using:
    * {{{
    *   import com.github.geirolz.advxml.instances._
    * }}}
    *
    * Otherwise you can import only a specific part of instances using:
    * {{{
    *   //import com.github.geirolz.advxml.implicits.[feature_name]._
    *   //example
    *   import com.github.geirolz.advxml.instances.transform._
    * }}}
    */
  object instances extends AllInstances {
    // format: off
    object transform  extends XmlTransformerInstances
    object convert    extends ConvertersInstances
    object normalize  extends XmlNormalizerInstances
    object validate   extends ValidationInstance
    // format: on
  }
}
