package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.convert.impls.TextConverter
import com.github.geirolz.advxml.convert.impls.TextConverter.TextConverter
import com.github.geirolz.advxml.transform.actions._
import com.github.geirolz.advxml.validate.MonadEx

import scala.xml.NodeSeq
import scala.xml.transform.{BasicTransformer, RewriteRule}

/**
  * Advxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTransformerSyntax extends RuleSyntax with ModifierSyntax with ZoomSyntax {

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform[F[_]: MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      XmlTransformer.transform(root, rule +: rules)

    def transform[F[_]: MonadEx](modifier: XmlModifier): F[NodeSeq] =
      XmlTransformer.transform(root, modifier)

    def transform[F[_]: MonadEx](f: Seq[RewriteRule] => BasicTransformer)(rules: XmlRule*): F[NodeSeq] =
      XmlTransformer.transform(f)(root, rules.toSeq)
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

private[transform] sealed trait ModifierSyntax {
  implicit class AttributeDataBuilder(q: String) {
    def :=[T: TextConverter](v: T): AttributeData = AttributeData(q, TextConverter(v))
  }
}

private[transform] sealed trait ZoomSyntax {

  implicit class XmlZoomOps(z: XmlZoom) {
    def \(that: XmlZoom): XmlZoom = z.andThen(that)
  }
}
