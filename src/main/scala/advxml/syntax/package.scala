package advxml

import advxml.core.validate.{EitherEx, ValidatedNelEx}

import scala.util.Try

/*
 * In order to keep project clean keep in mind the following rules:
 * - Each feature can provide a trait that contains ONLY the syntax, NO public object or explicit public class.
 * - Each object represent a feature
 * - Each feature must provide a trait containing all syntax implicits named `[feature_name]Syntax`
 * - For each object must be exist a package with the same name under `advxml`
 */
/**
  * This object is the entry point to access to all syntax implicits provided by Advxml.
  *
  * You can import all implicits using:
  * {{{
  *   import advxml.implicits._
  * }}}
  *
  * Otherwise you can import only a specific part of implicits using:
  * {{{
  *   //import advxml.implicits.[feature_name]._
  *   //example
  *   import advxml.implicits.transform._
  * }}}
  */
package object syntax {
  // format: off
  object all          extends AllSyntax
  object transform    extends XmlTransformerSyntax
  object convert      extends ConvertersSyntax
  object normalize    extends XmlNormalizerSyntax
  object validate     extends ValidationSyntax
  object traverse     extends XmlTraverserSyntax{
    object try_       extends XmlTraverserSyntaxSpecified[Try, Option]
    object option     extends XmlTraverserSyntaxSpecified[Option, Option]
    object either     extends XmlTraverserSyntaxSpecified[EitherEx, Option]
    object validated  extends XmlTraverserSyntaxSpecified[ValidatedNelEx, Option]
  }
  object predicate    extends PredicateSyntax
  object nestedMap    extends NestedMapSyntax
  // format: on
}
