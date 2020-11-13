package advxml.syntax

import advxml.core.transform._
import advxml.core.transform.actions.{ComposableXmlModifier, FinalXmlModifier, XmlZoom}
import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import advxml.core.validate.MonadEx
import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private[syntax] trait XmlTransformerSyntax extends RuleSyntax with ZoomSyntax {

  implicit class XmlTransformerOps(root: NodeSeq) {
    def transform[F[_]: MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      XmlTransformer.transform(root, rule +: rules)
  }
}

private[syntax] sealed trait RuleSyntax {

  implicit class XmlZoomToRuleOps(zoom: XmlZoom) {
    def withModifier(modifier: FinalXmlModifier): FinalXmlRule = XmlRule(zoom, modifier)
    def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule = XmlRule(zoom, List(modifier))

    def ==>(modifier: FinalXmlModifier): FinalXmlRule = zoom.withModifier(modifier)
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = zoom.withModifier(modifier)
  }

  implicit class ModifierCompatibleOps(r: ComposableXmlRule) {
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }
}

private[syntax] sealed trait ZoomSyntax {

  implicit class XmlZoomOps(z: XmlZoom) {

    def \(nodeName: String): XmlZoom =
      z.immediateDown(nodeName)

    def |(p: XmlPredicate): XmlZoom =
      z.filter(p)
  }
}
