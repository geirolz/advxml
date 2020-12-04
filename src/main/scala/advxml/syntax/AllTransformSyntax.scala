package advxml.syntax

import advxml.core.{ExHandler, MonadEx}
import advxml.core.data.{StringTo, XmlPredicate}
import advxml.core.transform._
import cats.Monad

import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private[syntax] trait AllTransformSyntax extends RuleSyntax with ZoomSyntax

private[syntax] sealed trait RuleSyntax {

  implicit class XmlNodeSeqTransformerOps(root: NodeSeq) {

    def transform[F[_]: MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      XmlRule.transform(root, rule, rules: _*)

    def transform[F[_]: MonadEx](rules: List[XmlRule]): F[NodeSeq] =
      XmlRule.transform(root, rules)
  }

  implicit class XmlRuleOps(rule: XmlRule) {
    def transform[F[_]: MonadEx](root: NodeSeq): F[NodeSeq] =
      XmlRule.transform(root, rule)
  }

  implicit class XmlSeqRuleOps(rules: List[XmlRule]) {
    def transform[F[_]: MonadEx](root: NodeSeq): F[NodeSeq] =
      XmlRule.transform(root, rules)
  }

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

  import advxml.instances.convert.identityConverter

  implicit class XmlZoomNodeBaseOps[Z <: XmlZoomNodeBase](zoom: Z) {

    def /(nodeName: String): Z#Type = zoom.immediateDown(nodeName)

    def |(p: XmlPredicate): Z#Type = zoom.filter(p)
  }

  implicit class XmlZoomOps(zoom: XmlZoom) {

    def /@[F[_]: Monad: ExHandler](key: String): NodeSeq => F[String] =
      ns => XmlContentZoom.attrM[F, String](zoom.run[F](ns), key)

    def textM[F[_]: Monad: ExHandler](implicit dummyImplicit: DummyImplicit): NodeSeq => F[String] =
      ns => XmlContentZoom.textM[F, String](zoom.run[F](ns))

    def /@[F[_]: Monad: ExHandler, T: StringTo[F, *]](key: String): NodeSeq => F[T] =
      ns => XmlContentZoom.attrM[F, T](zoom.run[F](ns), key)

    def textM[F[_]: Monad: ExHandler, T: StringTo[F, *]]: NodeSeq => F[T] =
      ns => XmlContentZoom.textM[F, T](zoom.run[F](ns))
  }

  implicit class BindedXmlZoomOps(zoom: BindedXmlZoom) {

    def /@[F[_]: Monad: ExHandler](key: String): F[String] =
      XmlContentZoom.attrM[F, String](zoom.run[F], key)

    def textM[F[_]: Monad: ExHandler]: F[String] =
      XmlContentZoom.textM[F, String](zoom.run[F])

    def /@[F[_]: Monad: ExHandler, T: StringTo[F, *]](key: String): F[T] =
      XmlContentZoom.attrM[F, T](zoom.run[F], key)

    def textM[F[_]: Monad: ExHandler, T: StringTo[F, *]]: F[T] =
      XmlContentZoom.textM[F, T](zoom.run[F])
  }

  implicit class XmlContentZoomOpsForId(ns: NodeSeq) {

    def /@[F[_]: Monad: ExHandler](key: String): F[String] =
      XmlContentZoom.attr[F, String](ns, key)

    def textM[F[_]: Monad: ExHandler]: F[String] =
      XmlContentZoom.text[F, String](ns)

    def /@[F[_]: Monad: ExHandler, T: StringTo[F, *]](key: String): F[T] =
      XmlContentZoom.attr[F, T](ns, key)

    def textM[F[_]: Monad: ExHandler, T: StringTo[F, *]]: F[T] =
      XmlContentZoom.text[F, T](ns)
  }

  implicit class XmlContentZoomOpsForMonad[F[_]: Monad: ExHandler](ns: F[NodeSeq]) {

    def /@(key: String): F[String] =
      XmlContentZoom.attrM[F, String](ns, key)

    def textM: F[String] =
      XmlContentZoom.textM[F, String](ns)

    def /@[T: StringTo[F, *]](key: String): F[T] =
      XmlContentZoom.attrM[F, T](ns, key)

    def textM[T: StringTo[F, *]]: F[T] =
      XmlContentZoom.textM[F, T](ns)
  }
}
