package advxml.syntax

import advxml.data.*
import advxml.transform.*
import cats.MonadThrow

import scala.xml.NodeSeq

/** Advxml Created by geirolad on 18/06/2019.
  *
  * @author
  *   geirolad
  */
private[syntax] trait AllTransformSyntax extends RuleSyntax with ZoomSyntax with NormalizerSyntax

private[syntax] sealed trait RuleSyntax {

  implicit class XmlNodeSeqTransformerOps(root: NodeSeq) {

    def transform[F[_]: MonadThrow](rule: AbstractRule, rules: AbstractRule*): F[NodeSeq] =
      AbstractRule.transform(root, rule, rules*)

    def transform[F[_]: MonadThrow](rules: List[AbstractRule]): F[NodeSeq] =
      AbstractRule.transform(root, rules)
  }

  implicit class AbstractRuleOps(rule: AbstractRule) {
    def transform[F[_]: MonadThrow](root: NodeSeq): F[NodeSeq] =
      AbstractRule.transform(root, rule)

    def and(other: AbstractRule): AbstractRule =
      AbstractRule.And(rule, other)

    def orElse(other: AbstractRule): AbstractRule =
      AbstractRule.OrElse(rule, other)

    def optional: AbstractRule =
      AbstractRule.Optional(rule)
  }

  implicit class AbstractRuleListOps(rules: List[AbstractRule]) {
    def transform[F[_]: MonadThrow](root: NodeSeq): F[NodeSeq] =
      AbstractRule.transform(root, rules)
  }

  implicit class XmlZoomToRuleOps(zoom: XmlZoom) {
    def withModifier(modifier: FinalXmlModifier): FinalXmlRule = XmlRule(zoom, modifier)

    def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      XmlRule(zoom, List(modifier))

    def ==>(modifier: FinalXmlModifier): FinalXmlRule = zoom.withModifier(modifier)

    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = zoom.withModifier(modifier)
  }

  implicit class ModifierCompatibleOps(r: ComposableXmlRule) {
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }
}

private[syntax] sealed trait ZoomSyntax {

  implicit class XmlZoomNodeBaseOps[Z <: XmlZoomNodeBase](val zoom: Z) {

    def /(nodeName: String): zoom.Type = zoom.down(nodeName)

    def |(p: XmlPredicate): zoom.Type = zoom.filter(p)
  }

  implicit class XmlContentZoomSyntaxForId(ns: NodeSeq) {

    def label: SimpleValue =
      XmlContentZoom.label(ns)

    def attr(key: String): ValidatedValue =
      XmlContentZoom.attr(ns, key)

    def content: ValidatedValue =
      XmlContentZoom.content(ns)
  }

  implicit class XmlContentZoomSyntaxForBindedXmlZoom(zoom: BindedXmlZoom) {

    def label: XmlContentZoomRunner =
      XmlContentZoom.labelFromBindedZoom(zoom)

    def attr(key: String): XmlContentZoomRunner =
      XmlContentZoom.attrFromBindedZoom(zoom, key)

    def content: XmlContentZoomRunner =
      XmlContentZoom.contentFromBindedZoom(zoom)
  }

  implicit class XmlContentZoomSyntaxForXmlZoom(zoom: XmlZoom) {

    def label(ns: NodeSeq): XmlContentZoomRunner =
      XmlContentZoom.labelFromZoom(zoom, ns)

    def attr(ns: NodeSeq, key: String): XmlContentZoomRunner =
      XmlContentZoom.attrFromZoom(zoom, ns, key)

    def content(ns: NodeSeq): XmlContentZoomRunner =
      XmlContentZoom.contentFromZoom(zoom, ns)
  }
}

private[syntax] trait NormalizerSyntax {

  implicit class NodeSeqNormalizationAndEqualityOps[N <: NodeSeq](ns: N) {

    def normalize: NodeSeq =
      XmlNormalizer.normalize(ns)

    def normalizedEquals(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |==|(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |!=|(ns2: NodeSeq): Boolean =
      !XmlNormalizer.normalizedEquals(ns, ns2)
  }
}
