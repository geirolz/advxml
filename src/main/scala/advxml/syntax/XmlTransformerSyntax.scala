package advxml.syntax

import advxml.core.transform._
import advxml.core.MonadEx
import advxml.core.data.XmlPredicate

import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private[syntax] trait XmlTransformerSyntax extends RuleSyntax with ZoomSyntax {

  implicit class XmlNodeSeqTransformerOps(root: NodeSeq) {

    def transform[F[_]: MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      XmlRule.transform(root, rule +: rules)

    def transform[F[_]: MonadEx](rules: List[XmlRule]): F[NodeSeq] =
      XmlRule.transform(root, rules)
  }

}

private[syntax] sealed trait RuleSyntax {

  implicit class XmlRuleOps(rule: XmlRule) {
    def transform[F[_]: MonadEx](root: NodeSeq): F[NodeSeq] =
      XmlRule.transform(root, rule)
  }

  implicit class XmlSeqRuleOps(rules: List[XmlRule]) {
    def transform[F[_]: MonadEx](root: NodeSeq): F[NodeSeq] =
      XmlRule.transform(root, rules)
  }

  implicit class ModifierCompatibleOps(r: ComposableXmlRule) {
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }

}

private[syntax] sealed trait ZoomSyntax {

  implicit class XmlZoomOps(zoom: XmlZoom) {

    def \(nodeName: String): XmlZoom = zoom.immediateDown(nodeName)

    def |(p: XmlPredicate): XmlZoom = zoom.filter(p)

    def withModifier(modifier: FinalXmlModifier): FinalXmlRule = XmlRule(zoom, modifier)

    def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule = XmlRule(zoom, List(modifier))

    def ==>(modifier: FinalXmlModifier): FinalXmlRule = zoom.withModifier(modifier)

    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = zoom.withModifier(modifier)
  }

}
