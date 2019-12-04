package com.github.geirolz.advxml.transform.actions

import cats.{Monoid, Semigroup}
import com.github.geirolz.advxml.transform.actions.ModifiersBuilders.{collapse, ExceptionF}
import com.github.geirolz.advxml.validate.MonadEx
import scala.xml._
import cats.syntax.flatMap._

sealed trait XmlModifier {
  private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq]
}
sealed trait FinalXmlModifier extends XmlModifier
sealed trait ComposableXmlModifier extends XmlModifier

private[advxml] trait ModifierInstances
    extends ModifiersTypeClassesInstances
    with ModifiersComposableInstances
    with ModifiersFinalInstances

private[actions] sealed trait ModifiersComposableInstances {

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
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
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
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
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
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(f(ns))
  }

  /**
    * Append attributes to current node.
    *
    * Supported only for `Node` elements, in other case will fail.
    * @param d Attribute data.
    * @param ds Attributes data.
    */
  case class SetAttrs(d: AttributeData, ds: AttributeData*) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
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
  case class RemoveAttrs(p: AttributeData => Boolean, ps: AttributeData => Boolean*) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          val newAttrs = e.attributes.asAttrMap
            .filter(_ match {
              case (k, v) => p(AttributeData(k, Text(v)))
            })
            .keys
            .foldLeft(e.attributes)((attrs, key) => attrs.remove(key))

          F.pure[NodeSeq](e.copy(attributes = newAttrs))
        case o => ExceptionF.unsupported[F](this, o)
      })
  }
}

private[actions] sealed trait ModifiersFinalInstances {

  /**
    * Remove selected nodes.
    */
  case object Remove extends FinalXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(NodeSeq.Empty)
  }
}

private[actions] sealed trait ModifiersTypeClassesInstances { this: ModifiersComposableInstances =>

  sealed trait ComposableXmlModifierSemigroup extends Semigroup[ComposableXmlModifier] {
    override def combine(x: ComposableXmlModifier, y: ComposableXmlModifier): ComposableXmlModifier =
      new ComposableXmlModifier {
        override def apply[F[_]: MonadEx](ns: NodeSeq): F[NodeSeq] =
          x.apply[F](ns).flatMap(y.apply[F](_))
      }
  }

  sealed trait ComposableXmlModifierMonoid extends Monoid[ComposableXmlModifier] with ComposableXmlModifierSemigroup {
    override def empty: ComposableXmlModifier = NoAction
  }

  implicit val composableXmlModifierSemigroupInstance: Semigroup[ComposableXmlModifier] =
    new ComposableXmlModifierSemigroup {}

  implicit val composableXmlModifierMonoidInstance: Monoid[ComposableXmlModifier] =
    new ComposableXmlModifierMonoid {}
}

private[actions] object ModifiersBuilders {

  def collapse[F[_]: MonadEx](seq: Seq[F[NodeSeq]]): F[NodeSeq] = {
    import cats.implicits._
    seq.toList.sequence.map(_.reduce(_ ++ _))
  }

  protected[actions] object ExceptionF {

    def apply[F[_]](msg: String)(implicit F: MonadEx[F]): F[NodeSeq] =
      F.raiseError[NodeSeq](new RuntimeException(msg))

    def unsupported[F[_]: MonadEx](modifier: XmlModifier, ns: NodeSeq): F[NodeSeq] =
      ExceptionF[F](s"Unsupported operation $modifier for type ${ns.getClass.getName}")
  }

}

//DATA
case class AttributeData(key: String, value: Text)
