package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.transform.actions._

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer extends XmlTransformerActions { $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform[F[_] : MonadEx](rule: XmlRule, rules: XmlRule*): F[NodeSeq] =
      $this.transform(rule, rules: _*)(root)

    def transform[F[_] : MonadEx](modifier: XmlModifier): F[NodeSeq] = modifier match {
      case m: ComposableXmlModifier => $this.transform(current(m))(root)
      case m: FinalXmlModifier => $this.transform(current(m))(root)
    }
  }

  def current(modifier: FinalXmlModifier) : FinalXmlRule =
    PartialXmlRule(identity) withModifier modifier

  def current(modifier: ComposableXmlModifier) : ComposableXmlRule =
    PartialXmlRule(identity) withModifier modifier


  def transform[F[_] : MonadEx](rule: XmlRule, rules: XmlRule*)(root: NodeSeq) : F[NodeSeq] = {

    import cats.implicits._

    (Seq(rule) ++ rules)
      .map(_.toRewriteRule[F](root))
      .toList
      .sequence
      .map(rules => new RuleTransformer(rules: _*).transform(root))
  }
}

private [transform] sealed trait XmlTransformerActions
  extends Modifiers
    with Zooms
    with Filters

