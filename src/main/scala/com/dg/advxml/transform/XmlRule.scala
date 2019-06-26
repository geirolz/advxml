package com.dg.advxml.transform

import com.dg.advxml.transform.actions.{Filters, XmlModifier, XmlZoom}

import scala.xml.transform.RewriteRule
import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */

sealed trait PartialXmlRule{
  val zoom: XmlZoom
  def withModifier(modifier: XmlModifier): XmlRule
}
sealed trait XmlRule extends PartialXmlRule{
  val modifier: XmlModifier
  def toRewriteRule: NodeSeq => RewriteRule
}

private [transform] object XmlRule{

  def apply(zoom: XmlZoom): PartialXmlRule = PartialXmlRuleImpl(zoom)

  private case class PartialXmlRuleImpl(zoom: XmlZoom) extends PartialXmlRule{
    override def withModifier(modifier: XmlModifier): XmlRule = XmlRuleImpl(zoom, modifier)
  }

  private case class XmlRuleImpl(zoom: XmlZoom, modifier: XmlModifier) extends XmlRule {

    override def withModifier(modifier: XmlModifier): XmlRule =
      copy(modifier = this.modifier.andThen(modifier))

    override def toRewriteRule: NodeSeq => RewriteRule = root => {

      val target = zoom(root)
      val updated = modifier(target)

      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || Filters.equalsTo(target)(ns)) updated else ns
      }
    }
  }
}