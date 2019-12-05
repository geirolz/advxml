package advxml.instances.transformation

import advxml.core.transformation.actions.{AttributeData, ComposableXmlModifier, FinalXmlModifier, XmlModifier}
import advxml.core.validation.MonadEx
import advxml.core.Predicate
import cats.{Monoid, Semigroup}
import cats.syntax.flatMap._

import scala.xml._

private[instances] trait AllXmlModifierInstances extends XmlModifierInstances with XmlModifierTypeClassesInstances

private[advxml] trait XmlModifierInstances {

  /**
    * No-Action modifiers, equals to `Replace` passing an identity function.
    */
  lazy val NoAction: Replace = Replace(identity[NodeSeq])

  /**
    * Prepend nodes to current nodes.
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

  /**
    * Append nodes to current nodes.
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

  /**
    * Replace current nodes.
    * @param f Function to from current nodes to new nodes.
    */
  case class Replace(f: NodeSeq => NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(f(ns))
  }

  /**
    * Append attributes to current node.
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
              attributes = (d +: ds).foldRight(e.attributes)(
                (data, metadata) => new UnprefixedAttribute(data.key, data.value, metadata)
              )
            )
          )
        case o => ExceptionF.unsupported[F](this, o)
      })
  }

  /**
    * Remove attributes.
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
            .filter {
              case (k, v) => filter(AttributeData(k, Text(v)))
            }
            .keys
            .foldLeft(e.attributes)((attrs, key) => attrs.remove(key))

          F.pure[NodeSeq](e.copy(attributes = newAttrs))
        case o => ExceptionF.unsupported[F](this, o)
      })
    }
  }

  /**
    * Remove selected nodes.
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

private[instances] trait XmlModifierTypeClassesInstances {

  sealed trait ComposableXmlModifierSemigroup extends Semigroup[ComposableXmlModifier] {
    override def combine(x: ComposableXmlModifier, y: ComposableXmlModifier): ComposableXmlModifier =
      new ComposableXmlModifier {
        override def apply[F[_]: MonadEx](ns: NodeSeq): F[NodeSeq] =
          x.apply[F](ns).flatMap(y.apply[F](_))
      }
  }

  sealed trait ComposableXmlModifierMonoid extends Monoid[ComposableXmlModifier] with ComposableXmlModifierSemigroup {
    override def empty: ComposableXmlModifier = advxml.instances.transform.NoAction
  }

  implicit val composableXmlModifierSemigroupInstance: Semigroup[ComposableXmlModifier] =
    new ComposableXmlModifierSemigroup {}

  implicit val composableXmlModifierMonoidInstance: Monoid[ComposableXmlModifier] =
    new ComposableXmlModifierMonoid {}
}
