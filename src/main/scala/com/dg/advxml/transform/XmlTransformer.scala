package com.dg.advxml.transform

import cats.Traverse
import com.dg.advxml.transform.actions._

import scala.util.Try
import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer

private [advxml] trait XmlTransformer extends XmlTransformerActions { $this =>

  implicit class XmlTransformerOps(root: NodeSeq) {

    def transform(rule: XmlRule, rules: XmlRule*): Try[NodeSeq] =
      $this.transform(rule, rules: _*)(root)

    def transform(action: XmlModifier): Try[NodeSeq] = action match {
      case m: FinalXmlModifier => $this.transform(current(m))(root)
      case m: ComposableXmlModifier => $this.transform(current(m))(root)
    }
  }

  def current(modifier: FinalXmlModifier) : FinalXmlRule =
    PartialXmlRule(identity) withModifier modifier

  def current(modifier: ComposableXmlModifier) : ComposableXmlRule =
    PartialXmlRule(identity) withModifier modifier


  def transform(rule: XmlRule, rules: XmlRule*)(root: NodeSeq) : Try[NodeSeq] = {

    import cats.instances.list._
    import cats.instances.try_._

    Traverse[List]
      .sequence((Seq(rule) ++ rules).map(_.toRewriteRule(root)).toList)
      .map(rules => new RuleTransformer(rules: _*).transform(root))
  }
}

private [transform] sealed trait XmlTransformerActions
  extends Modifiers
    with Zooms
    with Filters

