package com.dg.advxml.transform

import com.dg.advxml.transform.funcs.{Actions, Filters, XmlAction, Zooms}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer
  extends RuleSyntax
    with Filters
    with Zooms
    with Actions{ $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {
    def transform(rule: Rule, rules: Rule*): NodeSeq =
      $this.transform(rule, rules: _*)(root)
  }

  implicit class XmlTransformerRootOps(root: NodeSeq) {
    def transform(action: XmlAction, actions: XmlAction*): NodeSeq =
      $this.transform(current(action, actions: _*))(root)
  }

  def transform(rule: Rule, rules: Rule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer((Seq(rule) ++ rules)
      .map(_.toRewriteRule(root)): _*)
      .transform(root)
}

