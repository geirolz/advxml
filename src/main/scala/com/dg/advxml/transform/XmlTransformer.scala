package com.dg.advxml.transform

import com.dg.advxml.transform.funcs.{XmlAction, XmlFuncsImpls, XmlFuncsSyntax}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer
  extends XmlFuncsImpls
    with XmlFuncsSyntax { $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform(rule: XmlRule, rules: XmlRule*): NodeSeq =
      $this.transform(rule, rules: _*)(root)

    def transform(action: XmlAction): NodeSeq =
      $this.transform(current(action))(root)
  }

  def transform(rule: XmlRule, rules: XmlRule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer((Seq(rule) ++ rules)
      .map(_.toRewriteRule(root)): _*)
      .transform(root)
}

