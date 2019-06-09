package com.dg.advxml.core

import com.dg.advxml.core.funcs.Filters

import scala.xml.transform.RewriteRule
import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
sealed abstract case class Rule(zoom: Zoom, actions: Seq[Action]) {

  def toRewriteRule: NodeSeq => RewriteRule = root => {
    val target = zoom(root)
    val action = actions.reduce((a1, a2) => a1.compose(a2))
    val updated = action(target)

    new RewriteRule {
      override def transform(ns: Seq[Node]): Seq[Node] =
        if(ns == root || Filters.equalsTo(target)(ns)) updated else ns
    }
  }
}

private[advxml] trait RuleSyntax{
  def $(z: Zoom)(actions: Action*): Rule = new Rule(z, actions){}
  def current(actions: Action*): Rule = $(r => r)(actions: _*)
}

