package com.github.geirolz.advxml.transform.actions

import cats.{Monoid, Semigroup}
import com.github.geirolz.advxml.convert.impls.TextConverter
import com.github.geirolz.advxml.convert.impls.TextConverter.TextConverter
import com.github.geirolz.advxml.transform.actions.ModifiersBuilders.{collapse, UnsupportedException}
import com.github.geirolz.advxml.validate.MonadEx
import scala.xml._

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

  lazy val NoAction: Replace = Replace(identity[NodeSeq])

  case class Append(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = e.child ++ newNs))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = g.nodes ++ newNs))
        case o        => UnsupportedException[F](this, o)
      })
  }

  case class Replace(f: NodeSeq => NodeSeq) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(f(ns))
  }

  case class SetAttrs(values: (String, Text)*) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          F.pure[NodeSeq](
            e.copy(
              attributes = values.foldRight(e.attributes)(
                (value, metadata) => new UnprefixedAttribute(value._1, value._2, metadata)
              )
            )
          )
        case o => UnsupportedException[F](this, o)
      })
  }

  case class RemoveAttrs(key: String, keys: String*) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          val newAttrs = (Seq(key) ++ keys).foldLeft(e.attributes)((attrs, key) => attrs.remove(key))
          F.pure[NodeSeq](e.copy(attributes = newAttrs))
        case o => UnsupportedException[F](this, o)
      })
  }
}

private[actions] sealed trait ModifiersFinalInstances {
  case object Remove extends FinalXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(NodeSeq.Empty)
  }
}

private[actions] sealed trait ModifiersTypeClassesInstances { this: ModifiersComposableInstances =>

  import cats.syntax.flatMap._

  sealed trait ComposableXmlModifierSemigroup extends Semigroup[ComposableXmlModifier] {
    override def combine(x: ComposableXmlModifier, y: ComposableXmlModifier): ComposableXmlModifier =
      new ComposableXmlModifier {
        override def apply[F[_]: MonadEx](ns: NodeSeq): F[NodeSeq] = {
          x.apply[F](ns).flatMap(y.apply[F](_))
        }
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
    import cats.implicits._ //TODO
    seq.toList.sequence.map(_.reduce(_ ++ _))
  }

  object UnsupportedException {
    def apply[F[_]](modifier: XmlModifier, ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      F.raiseError[NodeSeq](new RuntimeException(s"Unsupported operation $modifier for type ${ns.getClass.getName}"))
  }
}
