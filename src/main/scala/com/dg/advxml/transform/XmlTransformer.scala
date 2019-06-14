package com.dg.advxml.transform

import com.dg.advxml.transform.funcs.{Actions, Filters, Zooms}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

trait XmlTransformer
  extends RuleSyntax
    with Filters
    with Zooms
    with Actions{ $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {
    def transform(rules: Rule*): NodeSeq =
      $this.transform(rules: _*)(root)
  }

  implicit class XmlTransformerOps2(root: NodeSeq) {
    def transform(rules: Action*): NodeSeq =
      $this.transform($()(rules: _*))(root)
  }

  def transform(rules: Rule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer(rules.map(_.toRewriteRule(root)): _*)
      .transform(root)
}

