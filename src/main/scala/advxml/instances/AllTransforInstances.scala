package advxml.instances

import advxml.core.data.{Predicate, _}
import advxml.core.transform.{ComposableXmlModifier, FinalXmlModifier, XmlModifier, XmlZoom}
import advxml.core.transform.XmlZoom.root
import advxml.core.MonadEx
import cats.Monoid

import scala.util.Try
import scala.xml.{Elem, Group, Node, NodeSeq, Text, UnprefixedAttribute}

private[instances] trait AllTransforInstances
    extends XmlModifierInstances
    with XmlPredicateInstances
    with XmlZoomInstances

private[instances] trait XmlModifierInstances {

  //******************************************* TYPE CLASS INSTANCES ********************************************
  implicit val composableXmlModifierMonoidInstance: Monoid[ComposableXmlModifier] = new Monoid[ComposableXmlModifier] {

    import cats.syntax.flatMap._

    override def empty: ComposableXmlModifier = advxml.instances.transform.NoAction

    override def combine(x: ComposableXmlModifier, y: ComposableXmlModifier): ComposableXmlModifier =
      new ComposableXmlModifier {
        override def apply[F[_]: MonadEx](ns: NodeSeq): F[NodeSeq] =
          x.apply[F](ns).flatMap(y.apply[F](_))
      }
  }

  //******************************************* XML MODIFIER INSTANCES ********************************************
  /** No-Action modifiers, equals to `Replace` passing an identity function.
    */
  lazy val NoAction: ComposableXmlModifier = Replace(identity[NodeSeq])

  /** Prepend nodes to current nodes.
    * Supported only for `Node` and `Group` elements, in other case will fail.
    * @param newNs Nodes to prepend.
    */
  case class Prepend(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = newNs ++ e.child))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = newNs ++ g.nodes))
        case o        => ExceptionF.unsupported[F](this, o)
      })
  }

  /** Append nodes to current nodes.
    * Supported only for `Node` and `Group` elements, in other case will fail.
    * @param newNs Nodes to append.
    */
  case class Append(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = e.child ++ newNs))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = g.nodes ++ newNs))
        case o        => ExceptionF.unsupported[F](this, o)
      })
  }

  /** Replace current nodes.
    * @param f Function to from current nodes to new nodes.
    */
  case class Replace(f: NodeSeq => NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(f(ns))
  }

  /** Append attributes to current node.
    *
    * Supported only for `Node` elements, in other case will fail.
    * @param d Attribute data.
    * @param ds Attributes data.
    */
  case class SetAttrs(d: AttributeData, ds: AttributeData*) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          F.pure[NodeSeq](
            e.copy(
              attributes = (d +: ds).foldRight(e.attributes)((data, metadata) =>
                new UnprefixedAttribute(data.key.value, data.value, metadata)
              )
            )
          )
        case o => ExceptionF.unsupported[F](this, o)
      })
  }

  /** Remove attributes.
    *
    * Supported only for `Node` elements, in other case will fail.
    * @param p Attribute predicate.
    * @param ps Attribute predicates.
    */
  case class RemoveAttrs(p: AttributeData => Boolean, ps: (AttributeData => Boolean)*) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = {
      val filter = (p +: ps).reduce((p1, p2) => Predicate.or(p1, p2))
      collapse[F](ns.map {
        case e: Elem =>
          val newAttrs = e.attributes.asAttrMap
            .filter { case (k, v) =>
              filter(AttributeData(Key(k), Text(v)))
            }
            .keys
            .foldLeft(e.attributes)((attrs, key) => attrs.remove(key))

          F.pure[NodeSeq](e.copy(attributes = newAttrs))
        case o => ExceptionF.unsupported[F](this, o)
      })
    }
  }

  /** Remove selected nodes.
    */
  case object Remove extends FinalXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(NodeSeq.Empty)
  }

  private def collapse[F[_]: MonadEx](seq: Seq[F[NodeSeq]]): F[NodeSeq] = {
    import cats.implicits._
    seq.toList.sequence.map(_.reduce(_ ++ _))
  }

  private object ExceptionF {

    def apply[F[_]](msg: String)(implicit F: MonadEx[F]): F[NodeSeq] =
      F.raiseError[NodeSeq](new RuntimeException(msg))

    def unsupported[F[_]: MonadEx](modifier: XmlModifier, ns: NodeSeq): F[NodeSeq] =
      ExceptionF[F](s"Unsupported operation $modifier for type ${ns.getClass.getName}")
  }

}

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
    * @param value  [[KeyValuePredicate]] to filter attributes
    * @param values N [[KeyValuePredicate]] to filter attributes
    * @return Predicate for nodes of type `Node`
    */
  def attrs(value: KeyValuePredicate[String], values: KeyValuePredicate[String]*): XmlPredicate =
    (value +: values)
      .map(p => XmlPredicate(ns => p(ns \@ p.key.value)))
      .reduce(Predicate.and[NodeSeq])

  /** Create a [[XmlPredicate]] that can check if a NodeSeq contains a child with specified predicates
    *
    * @param label     Name of the child to find
    * @param predicate Predicate to check child
    * @return [[XmlPredicate]] that can check if a NodeSeq contains a child with specified predicates
    */
  def hasImmediateChild(label: String, predicate: XmlPredicate = alwaysTrue): XmlPredicate = { xml =>
    import cats.instances.try_._
    root(xml).immediateDown(label).run[Try].fold(_ => false, _.nodeSeq.exists(predicate))
  }

  /** Create an [[XmlPredicate]] that can check if two NodeSeq are strictly equals.
    *
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

  implicit val xmlZoomMonoid: Monoid[XmlZoom] = new Monoid[XmlZoom] {
    override def empty: XmlZoom = XmlZoom.empty

    override def combine(x: XmlZoom, y: XmlZoom): XmlZoom = x.addAll(y.actions)
  }
}
