package com.dg.advxml.transform

import com.dg.advxml.transform.presets.Filters

import scala.xml.transform.RewriteRule
import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */

trait PartialXmlRule{
  val zoom: XmlZoom
  def withAction(action: XmlAction): XmlRule
}

trait XmlRule extends PartialXmlRule{
  val action: XmlAction
  def toRewriteRule: NodeSeq => RewriteRule
}

trait XmlAction extends (NodeSeq => NodeSeq){
  def andThen(that: NodeSeq => NodeSeq) : XmlAction = xml => that(this(xml))
}

trait XmlZoom extends (NodeSeq => NodeSeq){
  def andThen(that: XmlZoom): XmlZoom = xml => that(this(xml))
}

trait XmlPredicate extends (NodeSeq => Boolean){

  def and(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) && that(xml)

  def or(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) || that(xml)
}

private [transform] object XmlRule{

  def apply(zoom: XmlZoom): PartialXmlRule = PartialXmlRuleImpl(zoom)

  private case class PartialXmlRuleImpl(zoom: XmlZoom) extends PartialXmlRule{
    override def withAction(action: XmlAction): XmlRule = XmlRuleImpl(zoom, action)
  }

  private case class XmlRuleImpl(zoom: XmlZoom, action: XmlAction) extends XmlRule {

    override def withAction(action: XmlAction): XmlRule =
      copy(action = this.action.andThen(action))

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