package advxml.instances

import advxml.core.transform.actions.XmlPredicate
import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.Predicate

import scala.xml.{Node, NodeSeq}

private[instances] trait XmlPredicateInstances {

  /** Always true predicate.
    */
  lazy val alwaysTrue: XmlPredicate = _ => true

  /** Filter nodes by text property.
    *
    * @param p Text predicate
    * @return
    */
  def text(p: String => Boolean): XmlPredicate = e => p(e.text)

  /** Filter nodes by label property.
    *
    * @param p Label predicate
    * @return Predicate for nodes of type `Node`
    */
  def label(p: String => Boolean): XmlPredicate = {
    case n: Node => p(n.label)
    case _       => false
  }

  /** Filter nodes by attributes.
    *
    * @param value  Tuple2 where first value represent the attribute key and the second
    *               value represent a predicate function on attribute's value.
    * @param values N predicates for attributes.
    * @return Predicate for nodes of type `Node`
    */
  def attrs(value: (String, String => Boolean), values: (String, String => Boolean)*): XmlPredicate = {
    (value +: values)
      .map { case (key, p) =>
        XmlPredicate(ns => p(ns \@ key))
      }
      .reduce(Predicate.and[NodeSeq])
  }

  def hasImmediateChild(label: String, predicate: XmlPredicate = alwaysTrue): XmlPredicate = { xml =>
    import cats.instances.option._
    import advxml.syntax.traverse.option._
    (xml \? label).fold(false)(_.exists(predicate))
  }

  def strictEqualsTo(ns: NodeSeq): XmlPredicate =
    that =>
      (ns, that) match {
        case (e1: Node, e2: Node)         => e1 strict_== e2
        case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
      }
}
