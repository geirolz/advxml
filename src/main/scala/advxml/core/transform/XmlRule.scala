package advxml.core.transform

import advxml.core.transform.actions.{ComposableXmlModifier, FinalXmlModifier, XmlModifier}
import advxml.core.transform.actions.XmlZoom.XmlZoom
import advxml.core.validate.MonadEx
import cats.kernel.Monoid

import scala.xml.{Node, NodeSeq}
import scala.xml.transform.RewriteRule

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
sealed trait PartialXmlRule extends ModifierComposableXmlRule {
  val zoom: XmlZoom
  def withModifier(modifier: FinalXmlModifier): FinalXmlRule
}

sealed trait XmlRule {
  val zoom: XmlZoom

  import cats.syntax.functor._
  import advxml.instances.transform._

  final def toRewriteRule[F[_]: MonadEx](root: NodeSeq): F[RewriteRule] =
    (this match {
      case r: ComposableXmlRule => RewriteRuleBuilder(Monoid.combineAll(r.modifiers))
      case r: FinalXmlRule      => RewriteRuleBuilder[F](r.modifier)
    })(zoom, root)

  private object RewriteRuleBuilder {

    def apply[F[_]: MonadEx](modifier: XmlModifier): (XmlZoom, NodeSeq) => F[RewriteRule] =
      (zoom, root) => {
        val target = zoom(root)

        modifier[F](target).map(updated => {
          new RewriteRule {
            override def transform(ns: collection.Seq[Node]): collection.Seq[Node] =
              if (ns == root || strictEqualsTo(target)(ns)) updated else ns
          }
        })
      }
  }
}

sealed trait ComposableXmlRule extends XmlRule with ModifierComposableXmlRule {
  val modifiers: Seq[ComposableXmlModifier]
}

sealed trait FinalXmlRule extends XmlRule {
  val modifier: FinalXmlModifier
}

private[transform] sealed trait ModifierComposableXmlRule {
  def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule
}

object PartialXmlRule {

  def apply(zoom: XmlZoom): PartialXmlRule = PartialXmlRuleImpl(zoom)

  private case class PartialXmlRuleImpl(zoom: XmlZoom) extends PartialXmlRule {
    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      ComposableXmlRuleImpl(zoom, Seq(modifier))

    override def withModifier(modifier: FinalXmlModifier): FinalXmlRule =
      FinalXmlRuleImpl(zoom, modifier)
  }

  private case class ComposableXmlRuleImpl(zoom: XmlZoom, modifiers: Seq[ComposableXmlModifier])
      extends ComposableXmlRule {

    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      copy(modifiers = modifiers :+ modifier)
  }

  private case class FinalXmlRuleImpl(zoom: XmlZoom, modifier: FinalXmlModifier) extends FinalXmlRule
}
