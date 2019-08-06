package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.transform.actions._
import com.github.geirolz.advxml.utils.PredicateUtils

import scala.xml.NodeSeq
import scala.xml.transform.{BasicTransformer, RewriteRule}

/**
  * Advxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTransformerSyntax
    extends RuleSyntax
    with ModifiersSyntax
    with ZoomSyntax
    with PredicateSyntax {

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform[F[_]: MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      XmlTransformer.transform(root, rule +: rules)

    def transform[F[_]: MonadEx](modifier: XmlModifier): F[NodeSeq] =
      XmlTransformer.transform(root, modifier)

    def transform[F[_]: MonadEx](f: Seq[RewriteRule] => BasicTransformer)(rules: XmlRule*): F[NodeSeq] =
      XmlTransformer.transform(f)(root, rules)
  }

}

private[transform] sealed trait RuleSyntax {

  def $(zoom: XmlZoom): PartialXmlRule = PartialXmlRule(zoom)

  implicit class PartialRuleOps(r: PartialXmlRule) {
    def ==>(modifier: FinalXmlModifier): FinalXmlRule = r.withModifier(modifier)
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }

  implicit class ModifierCompatibleOps(r: ComposableXmlRule) {
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }
}

private[transform] sealed trait ModifiersSyntax {

  implicit class ComposableXmlModifierOps(a: ComposableXmlModifier) {
    def ++(that: ComposableXmlModifier): ComposableXmlModifier = a.andThen(that)
  }
}

private[transform] sealed trait ZoomSyntax {

  implicit class XmlZoomOps(z: XmlZoom) {
    def \(that: XmlZoom): XmlZoom = z.andThen(that)
  }
}

private[transform] sealed trait PredicateSyntax {

  implicit class XmlPredicateOps(p: XmlPredicate) {

    def &&(that: XmlPredicate): XmlPredicate = p.and(that)

    def and(that: XmlPredicate): XmlPredicate = PredicateUtils.and(p, that)

    def ||(that: XmlPredicate): XmlPredicate = p.or(that)

    def or(that: XmlPredicate): XmlPredicate = PredicateUtils.or(p, that)
  }
}
