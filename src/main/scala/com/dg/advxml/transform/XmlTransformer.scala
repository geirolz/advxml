package com.dg.advxml.transform

import com.dg.advxml.transform.funcs.{Actions, Filters, Rule, RuleSyntax, Rules, XmlAction, ZoomSyntax, Zooms}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer
  extends Rules
    with Filters
    with Zooms
    with Actions
    with RuleSyntax
    with ZoomSyntax{ $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform(rule: Rule, rules: Rule*): NodeSeq =
      $this.transform(rule, rules: _*)(root)

    def transform(action: XmlAction): NodeSeq =
      $this.transform(current(action))(root)
  }

  def transform(rule: Rule, rules: Rule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer((Seq(rule) ++ rules)
      .map(_.toRewriteRule(root)): _*)
      .transform(root)
}

