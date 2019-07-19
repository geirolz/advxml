package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.transform.actions.{ComposableXmlModifier, XmlModifier, XmlZoom, _}

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

  final def toRewriteRule[F[_]: MonadEx](root: NodeSeq): F[RewriteRule] =
    (this match {
      case r: ComposableXmlRule => RewriteRuleBuilder(r.modifiers.reduce((m1, m2) => m1.andThen(m2)))
      case r: FinalXmlRule      => RewriteRuleBuilder[F](r.modifier)
    })(zoom, root)

  private object RewriteRuleBuilder {

    def apply[F[_]: MonadEx](modifier: XmlModifier): (XmlZoom, NodeSeq) => F[RewriteRule] =
      (zoom, root) => {

        import cats.implicits._

        val target = zoom(root)

        modifier[F](target).map(updated => {
          new RewriteRule {
            override def transform(ns: collection.Seq[Node]): collection.Seq[Node] =
              if (ns == root || Filters.equalsTo(target)(ns)) updated else ns
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
      copy(modifiers = Seq(modifier) ++ modifiers)
  }

  private case class FinalXmlRuleImpl(zoom: XmlZoom, modifier: FinalXmlModifier) extends FinalXmlRule
}
