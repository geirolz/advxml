package com.dg.advxml.transform

import com.dg.advxml.transform.actions.{Actions, Filters, XmlAction, Zooms}

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer extends XmlTransformerActions { $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform(rule: XmlRule, rules: XmlRule*): NodeSeq =
      $this.transform(rule, rules: _*)(root)

    def transform(action: XmlAction): NodeSeq =
      $this.transform(current(action))(root)
  }

  def current(action: XmlAction) : XmlRule =
    XmlRule(identity(_)) withAction action

  def transform(rule: XmlRule, rules: XmlRule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer((Seq(rule) ++ rules)
      .map(_.toRewriteRule(root)): _*)
      .transform(root)
}

private [transform] sealed trait XmlTransformerActions
  extends Actions
    with Zooms
    with Filters

