package advxml.data

import advxml.data.Predicate.alwaysTrue
import advxml.transform.XmlZoom
import cats.data.NonEmptyList

import scala.util.Try
import scala.xml.{Node, NodeSeq}

object XmlPredicate {

  import cats.instances.try_.*

  def apply(f: XmlPredicate): XmlPredicate = f

  /** Filter nodes by text property.
    *
    * @param p
    *   Text predicate
    * @return
    */
  def text(p: String => Boolean): XmlPredicate = e => p(e.text)

  /** Filter nodes by label property.
    *
    * @param p
    *   Label predicate
    * @return
    *   Predicate for nodes of type `Node`
    */
  def label(p: String => Boolean): XmlPredicate = {
    case n: Node => p(n.label)
    case _       => false
  }

  /** Check if node has all attributes.
    *
    * @param key
    *   [[advxml.data.Key]] to check
    * @param keys
    *   N [[advxml.data.Key]] list to check
    * @return
    *   Predicate for nodes of type `Node`
    */
  def hasAttrs(key: Key, keys: Key*): XmlPredicate =
    hasAttrs(NonEmptyList.of(key, keys*))

  /** Check if node has all attributes.
    *
    * @param keys
    *   [[advxml.data.Key]] list to check
    * @return
    *   Predicate for nodes of type `Node`
    */
  def hasAttrs(keys: NonEmptyList[Key]): XmlPredicate =
    attrs(keys.map(k => KeyValuePredicate(k, _.nonEmpty.extract[Option].isDefined)))

  /** Filter nodes by attributes.
    *
    * @param value
    *   [[advxml.data.KeyValuePredicate]] to filter attributes
    * @param values
    *   N [[advxml.data.KeyValuePredicate]] to filter attributes
    * @return
    *   Predicate for nodes of type `Node`
    */
  def attrs(value: KeyValuePredicate, values: KeyValuePredicate*): XmlPredicate =
    this.attrs(NonEmptyList.of(value, values*))

  /** Filter nodes by attributes.
    *
    * @param values
    *   N [[advxml.data.KeyValuePredicate]] to filter attributes
    * @return
    *   Predicate for nodes of type `Node`
    */
  def attrs(values: NonEmptyList[KeyValuePredicate]): XmlPredicate =
    values
      .map(p => XmlPredicate(ns => p(SimpleValue(ns \@ p.key.value))))
      .reduce(Predicate.and[NodeSeq](_, _))

  /** Create a [[advxml.data.XmlPredicate]] that can check if a NodeSeq contains a child with
    * specified predicates
    *
    * @param label
    *   Name of the child to find
    * @param predicate
    *   Predicate to check child
    * @return
    *   [[advxml.data.XmlPredicate]] that can check if a NodeSeq contains a child with specified
    *   predicates
    */
  def hasImmediateChild(label: String, predicate: XmlPredicate = alwaysTrue): XmlPredicate =
    XmlZoom
      .root(_)
      .down(label)
      .run[Try]
      .fold(_ => false, _.exists(predicate))

  /** Create an [[advxml.data.XmlPredicate]] that can check if two NodeSeq are strictly equals.
    *
    * @param ns
    *   to compare
    * @return
    *   [[advxml.data.XmlPredicate]] that can check if two NodeSeq are strictly equals.
    */
  def strictEqualsTo(ns: NodeSeq): XmlPredicate =
    that =>
      (ns, that) match {
        case (e1: Node, e2: Node)         => e1 strict_== e2
        case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
      }
}
