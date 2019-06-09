package com.dg.advxml

import scala.xml._
import scala.xml.transform.{RewriteRule, RuleTransformer}

object XmlAction {


  sealed abstract case class Rule(zoom: Zoom, actions: Seq[Action]) {

    def toRewriteRule: NodeSeq => RewriteRule = root => {
      val target = zoom(root)
      val action = actions.reduce((a1, a2) => a1.compose(a2))
      val updated = action(target)

      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || filters.equalsTo(target)(ns)) updated else ns
      }
    }
  }

  def $(z: Zoom)(actions: Action*): Rule = new Rule(z, actions){}
  def current(actions: Action*): Rule = $(r => r)(actions: _*)

  implicit class AddXmlAction(rootElem: NodeSeq) {
    def transform(rules: Rule*): NodeSeq =
      new RuleTransformer(rules.map(_.toRewriteRule(rootElem)): _*)
        .transform(rootElem)

    def transform(actions: Action*): NodeSeq = transform(current(actions: _*))
  }
}
