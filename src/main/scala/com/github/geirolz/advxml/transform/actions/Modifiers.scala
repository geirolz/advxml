package com.github.geirolz.advxml.transform.actions

import com.github.geirolz.advxml.convert.XmlTextSerializer
import com.github.geirolz.advxml.convert.XmlTextSerializer.Serializer

import scala.xml._

sealed trait XmlModifier {
  private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq]
}
sealed trait FinalXmlModifier extends XmlModifier

sealed trait ComposableXmlModifier extends XmlModifier {
  $this =>

  def andThen(that: ComposableXmlModifier): ComposableXmlModifier = new ComposableXmlModifier {

    import cats.syntax.flatMap._

    override def apply[F[_]: MonadEx](ns: NodeSeq): F[NodeSeq] = $this(ns).flatMap(that(_))
  }
}

private[transform] trait Modifiers {

  import builders._

  //STDs
  case class Append(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem  => F.pure[NodeSeq](e.copy(child = e.child ++ newNs))
        case g: Group => F.pure[NodeSeq](g.copy(nodes = g.nodes ++ newNs))
        case o        => UnsupportedException[F](this, o)
      })
  }

  case class Replace(newNs: NodeSeq) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(newNs)
  }

  case class SetAttrs[T: Serializer](values: (String, T)*) extends ComposableXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
      collapse[F](ns.map {
        case e: Elem =>
          F.pure[NodeSeq](
            e.copy(
              attributes = values.foldRight(e.attributes)(
                (value, metadata) => new UnprefixedAttribute(value._1, XmlTextSerializer.asText(value._2), metadata)
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

  //FINALS
  case object Remove extends FinalXmlModifier {
    override private[transform] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = F.pure(NodeSeq.Empty)
  }

  private[actions] object builders {

    object UnsupportedException {

      def apply[F[_]](modifier: XmlModifier, ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] =
        F.raiseError[NodeSeq](new RuntimeException(s"Unsupported operation $modifier for type ${ns.getClass.getName}"))
    }

    def collapse[F[_]: MonadEx](seq: Seq[F[NodeSeq]]): F[NodeSeq] = {
      import cats.implicits._
      seq.toList.sequence.map(_.reduce(_ ++ _))
    }
  }
}

object Modifiers extends Modifiers
