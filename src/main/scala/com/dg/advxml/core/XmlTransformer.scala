package com.dg.advxml.core

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer


object XmlTransformer extends XmlTransformerImplicits{

  def transform(rules: Rule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer(rules.map(_.toRewriteRule(root)): _*)
      .transform(root)
}

private [advxml] trait XmlTransformerImplicits{

  implicit class XmlTransformerOps(root: NodeSeq) {
    def transform(rules: Rule*): NodeSeq =
      XmlTransformer.transform(rules: _*)(root)
  }

  implicit class XmlTransformerOps2(root: NodeSeq) {
    def transform(rules: Action*): NodeSeq =
      XmlTransformer.transform(RuleSyntax.$()(rules: _*))(root)
  }
}
