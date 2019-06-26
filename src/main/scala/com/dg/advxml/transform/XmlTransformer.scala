package com.dg.advxml.transform

import com.dg.advxml.transform.actions.{Modifiers, Filters, XmlModifier, Zooms}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer extends XmlTransformerActions { $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform(rule: XmlRule, rules: XmlRule*): NodeSeq =
      $this.transform(rule, rules: _*)(root)

    def transform(action: XmlModifier): NodeSeq =
      $this.transform(current(action))(root)
  }

  def current(modifier: XmlModifier) : XmlRule =
    XmlRule(identity) withModifier modifier

  def transform(rule: XmlRule, rules: XmlRule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer((Seq(rule) ++ rules)
      .map(_.toRewriteRule(root)): _*)
      .transform(root)
}

private [transform] sealed trait XmlTransformerActions
  extends Modifiers
    with Zooms
    with Filters

