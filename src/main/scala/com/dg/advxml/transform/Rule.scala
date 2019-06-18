package com.dg.advxml.transform

import com.dg.advxml.transform.funcs.{Filters, XmlAction, XmlZoom}

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
  def ==>(action: XmlAction, actions: XmlAction*): Rule
}

trait Rule extends PartialRule{
  val actions: Seq[XmlAction]
  def toRewriteRule: NodeSeq => RewriteRule
}

private [transform] trait Rules{

  def rule(zoom: XmlZoom): PartialRule = PartialRuleImpl(zoom)

  private case class PartialRuleImpl(zoom: XmlZoom) extends PartialRule{
    override def ==>(action: XmlAction, actions: XmlAction*): Rule =
      RuleImpl(zoom, Seq(action) ++ actions)
  }

  private case class RuleImpl(zoom: XmlZoom, actions: Seq[XmlAction]) extends Rule {

    override def ==>(action: XmlAction, actions: XmlAction*): Rule =
      copy(actions = this.actions ++ Seq(action) ++ actions)

    override def toRewriteRule: NodeSeq => RewriteRule = root => {

      val target = zoom(root)
      val action = actions.foldLeft(identity[NodeSeq](_))((acc, a) => acc.andThen(a))
      val updated = action(target)

      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || Filters.equalsTo(target)(ns)) updated else ns
      }
    }
  }
}

private [transform] trait RuleSyntax { this : Rules =>
  def current(action: XmlAction, actions: XmlAction*) : Rule = $(identity(_)) ==> (action, actions:_*)
  def $(zoom: XmlZoom): PartialRule = rule(zoom)
}

