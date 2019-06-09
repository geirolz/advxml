package com.dg.advxml.core

import com.dg.advxml.AdvXml.current

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer


object XmlTransformer extends XmlTransformerImplicits{

  def transform(rules: Rule*)(root: NodeSeq) : NodeSeq =
    new RuleTransformer(rules.map(_.toRewriteRule(root)): _*)
      .transform(root)
}

private [advxml] trait XmlTransformerImplicits{

  implicit class AddAdvXmlTransformation(root: NodeSeq) {
    def transform(rules: Rule*): NodeSeq = XmlTransformer.transform(rules: _*)(root)
    def transformCurrent(actions: Action*): NodeSeq = transform(current(actions: _*))
  }
}
