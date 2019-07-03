package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.transform.actions._

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

object XmlTransformer {

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


  object actions extends XmlTransformerActions

  object ops extends XmlTransformerSyntax

}

private[advxml] trait XmlTransformerActions
  extends Modifiers
    with Zooms
    with Filters
