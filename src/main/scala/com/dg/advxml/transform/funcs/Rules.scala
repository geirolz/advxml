package com.dg.advxml.transform.funcs

import scala.xml.transform.RewriteRule
import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */

trait PartialRule{
  val zoom: XmlZoom
  def ==>(action: XmlAction): Rule
}

trait Rule extends PartialRule{
  val action: XmlAction
  def toRewriteRule: NodeSeq => RewriteRule
}

private [transform] trait Rules{

  def rule(zoom: XmlZoom): PartialRule = PartialRuleImpl(zoom)

  private case class PartialRuleImpl(zoom: XmlZoom) extends PartialRule{
    override def ==>(action: XmlAction): Rule =
      RuleImpl(zoom, action)
  }

  private case class RuleImpl(zoom: XmlZoom, action: XmlAction) extends Rule {

    override def ==>(action: XmlAction): Rule =
      copy(action = this.action ++ action)

    override def toRewriteRule: NodeSeq => RewriteRule = root => {

      val target = zoom(root)
      val updated = action(target)

      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || Filters.equalsTo(target)(ns)) updated else ns
      }
    }
  }
}

private [transform] trait RuleSyntax { this : Rules =>
  def current(action: XmlAction) : Rule = $(identity(_)) ==> action
  def $(zoom: XmlZoom): PartialRule = rule(zoom)
}

