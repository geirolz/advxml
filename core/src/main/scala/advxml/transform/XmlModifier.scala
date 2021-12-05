package advxml.transform

import advxml.data.{AttributeData, Predicate}
import cats.{MonadThrow, Monoid}
import cats.data.NonEmptyList

import scala.xml.{Elem, Group, NodeSeq, UnprefixedAttribute}

sealed trait XmlModifier {
  private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq]
}
object XmlModifier extends XmlModifierValues with XmlModifierInstances

private[advxml] trait XmlModifierValues
    extends FinalXmlModifierValues
    with ComposableXmlModifierValues

private[advxml] trait XmlModifierInstances extends ComposableXmlModifierInstances

//============================= FINAL ==============================
trait FinalXmlModifier extends XmlModifier

object FinalXmlModifier extends FinalXmlModifierValues

private[transform] sealed trait FinalXmlModifierValues {

  /** Remove selected nodes.
    */
  case object Remove extends FinalXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] =
      F.pure(NodeSeq.Empty)
  }

}

//=========================== COMPOSABLE ===========================
trait ComposableXmlModifier extends XmlModifier

object ComposableXmlModifier extends ComposableXmlModifierValues with ComposableXmlModifierInstances

private[transform] sealed trait ComposableXmlModifierValues {

  /** No-Action modifiers, equals to `Replace` passing an identity function.
    */
  lazy val NoAction: ComposableXmlModifier = Replace(identity[NodeSeq])

  /** Prepend nodes to current nodes. Supported only for `Node` and `Group` elements, in other case
    * will fail.
    * @param newNs
    *   Nodes to prepend.
    */
  case class Prepend(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = newNs ++ e.child))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = newNs ++ g.nodes))
        case o        => ExceptionF.unsupported[F](this, o)
      })
  }

  /** Append nodes to current nodes. Supported only for `Node` and `Group` elements, in other case
    * will fail.
    * @param newNs
    *   Nodes to append.
    */
  case class Append(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = e.child ++ newNs))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = g.nodes ++ newNs))
        case o        => ExceptionF.unsupported[F](this, o)
      })
  }

  /** Replace current nodes.
    * @param f
    *   Function to from current nodes to new nodes.
    */
  case class Replace(f: NodeSeq => NodeSeq) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] =
      F.pure(f(ns))
  }

  /** Append or replace attributes to current node.
    *
    * Supported only for `Node` elements, in other case will fail.
    * @param f
    *   takes Elem (attribute container), returns Attributes data.
    */
  case class SetAttrs(f: Elem => NonEmptyList[AttributeData]) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          F.pure[NodeSeq](
            e.copy(
              attributes = f(e).toList.foldRight(e.attributes)((data, metadata) =>
                new UnprefixedAttribute(data.key.value, data.value.get, metadata)
              )
            )
          )
        case o => ExceptionF.unsupported[F](this, o)
      })

  }
  object SetAttrs {

    /** Create a SetAttrs attributes action with specified data.
      *
      * Supported only for `Node` elements, in other case will fail.
      * @param d
      *   Attribute data.
      * @param ds
      *   Attributes data.
      */
    def apply(d: AttributeData, ds: AttributeData*): SetAttrs =
      SetAttrs(_ => NonEmptyList.of(d, ds*))

  }

  object SetAttr {

    /** Create a SetAttrs attributes action with specified data.
      *
      * Supported only for `Node` elements, in other case will fail.
      * @param f
      *   takes the Elem (attribute container), returns Attribute data.
      */
    def apply(f: Elem => AttributeData): SetAttrs =
      SetAttrs.apply(el => NonEmptyList.one(f(el)))
  }

  /** Remove attributes.
    *
    * Supported only for `Node` elements, in other case will fail.
    * @param ps
    *   Attribute predicates.
    */
  case class RemoveAttrs(ps: NonEmptyList[AttributeData => Boolean]) extends ComposableXmlModifier {
    override private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq] = {
      val attrsToRemoveP = ps.reduce[AttributeData => Boolean]((p1, p2) => Predicate.or(p1, p2))
      collapse[F](ns.map {
        case e: Elem =>
          val newAttrs = AttributeData
            .fromElem(e)
            .filter(attrsToRemoveP)
            .map(_.key)
            .foldLeft(e.attributes)((attrs, key) => attrs.remove(key.value))

          F.pure[NodeSeq](e.copy(attributes = newAttrs))
        case o => ExceptionF.unsupported[F](this, o)
      })
    }
  }
  object RemoveAttrs {

    /** Create a Remove attributes action with specified filters.
      * @param p
      *   Attribute predicate.
      * @param ps
      *   Attribute predicates.
      */
    def apply(p: AttributeData => Boolean, ps: (AttributeData => Boolean)*): RemoveAttrs =
      RemoveAttrs(
        NonEmptyList.of(p, ps*)
      )
  }

  private def collapse[F[_]: MonadThrow](seq: Seq[F[NodeSeq]]): F[NodeSeq] = {
    import cats.implicits.*
    seq.toList.sequence.map(_.reduce(_ ++ _))
  }

  private object ExceptionF {

    def apply[F[_]](msg: String)(implicit F: MonadThrow[F]): F[NodeSeq] =
      F.raiseError[NodeSeq](new RuntimeException(msg))

    def unsupported[F[_]: MonadThrow](modifier: XmlModifier, ns: NodeSeq): F[NodeSeq] =
      ExceptionF[F](s"Unsupported operation $modifier for type ${ns.getClass.getName}")
  }
}

private[transform] sealed trait ComposableXmlModifierInstances {

  implicit val composableXmlModifierMonoidInstance: Monoid[ComposableXmlModifier] =
    new Monoid[ComposableXmlModifier] {

      import cats.syntax.flatMap.*

      override def empty: ComposableXmlModifier = ComposableXmlModifier.NoAction

      override def combine(
        x: ComposableXmlModifier,
        y: ComposableXmlModifier
      ): ComposableXmlModifier =
        new ComposableXmlModifier {
          override def apply[F[_]: MonadThrow](ns: NodeSeq): F[NodeSeq] =
            x.apply[F](ns).flatMap(y.apply[F](_))
        }
    }
}
