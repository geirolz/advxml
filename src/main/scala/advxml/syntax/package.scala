package advxml

/*
 * In order to keep project clean keep in mind the following rules:
 * - Each feature can provide a trait that contains ONLY the syntax, NO public object or explicit public class.
 * - Each object represent a feature
 * - Each feature must provide a trait containing all syntax implicits named `[feature_name]Syntax`
 * - For each object must be exist a package with the same name under `advxml`
 */
/** This object is the entry point to access to all syntax implicits provided by Advxml.
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
package object syntax extends AllCommonSyntax {
  // format: off
  object all              extends AllSyntax
  
  //******************** FEATURES ********************
  object transform        extends AllTransformSyntax
  object convert          extends ConvertersSyntax
  object validated        extends ValidatedSyntax
  object javaConverters   extends JavaScalaConvertersSyntax
  // format: on
}
