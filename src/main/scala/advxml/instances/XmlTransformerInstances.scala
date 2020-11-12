package advxml.instances

import advxml.core.transform.actions.XmlZoom
import cats.Monoid
import advxml.core.transform.actions.XmlPredicate
import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.Predicate

import scala.xml.{Node, NodeSeq}

private[instances] trait XmlTransformerInstances
    extends XmlModifierInstances
    with XmlPredicateInstances
    with XmlZoomInstances

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
    * @return Predicate for nodes of type [[Node]]
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

  /** Create a [[XmlPredicate]] that can check if a NodeSeq contains a child with specified predicates
    * @param label Name of the child to find
    * @param predicate Predicate to check child
    * @return [[XmlPredicate]] that can check if a NodeSeq contains a child with specified predicates
    */
  def hasImmediateChild(label: String, predicate: XmlPredicate = alwaysTrue): XmlPredicate = { xml =>
    import advxml.instances.traverse._
    import advxml.syntax.traverse.option._
    import cats.instances.option._

    (xml \? label).fold(false)(_.exists(predicate))
  }

  /** Create an [[XmlPredicate]] that can check if two NodeSeq are strictly equals.
    * @param ns to compare
    * @return [[XmlPredicate]] that can check if two NodeSeq are strictly equals.
    */
  def strictEqualsTo(ns: NodeSeq): XmlPredicate =
    that =>
      (ns, that) match {
        case (e1: Node, e2: Node)         => e1 strict_== e2
        case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
      }
}

private[instances] trait XmlZoomInstances {

  /** Just an alias for [[XmlZoom.root]], to use when you are building and XmlZoom that starts from the root.
    */
  lazy val root: XmlZoom = XmlZoom.root

  /** Just an alias for Root, to use when you are building and XmlZoom that not starts from the root for the document.
    * It's exists just to clarify the code.
    * If your [[XmlZoom]] starts for the root of the document please use [[root]]
    */
  lazy val > : XmlZoom = root

  implicit val xmlZoomMonoid: Monoid[XmlZoom] = new Monoid[XmlZoom] {
    override def empty: XmlZoom = XmlZoom.empty
    override def combine(x: XmlZoom, y: XmlZoom): XmlZoom = XmlZoom(x.zoomActions ++ y.zoomActions)
  }
}
