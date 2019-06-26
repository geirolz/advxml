package com.dg.advxml.transform

import com.dg.advxml.transform.actions.{Filters, XmlModifier, XmlPredicate, XmlZoom}

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlTransformerSyntax
  extends RuleSyntax
    with ActionsSyntax
    with ZoomSyntax
    with PredicateSyntax

private [transform] sealed trait RuleSyntax {

  def $(zoom: XmlZoom): PartialXmlRule = XmlRule(zoom)

  implicit class RuleOps[T <: PartialXmlRule](r: T) {
    def ==>(modifier: XmlModifier): XmlRule = r.withModifier(modifier)
  }
}

private [transform] sealed trait ActionsSyntax {

  implicit class XmlActionOps(a: XmlModifier) {
    def ++(that: XmlModifier) : XmlModifier = a.andThen(that)
  }
}

private [transform] sealed trait ZoomSyntax{

  implicit class XmlZoomOps(z: XmlZoom){
    def \(that: XmlZoom) : XmlZoom = z.andThen(that)
  }
}

private [transform] sealed trait PredicateSyntax {

  implicit class XmlPredicateOps(p: XmlPredicate){

    import actions._

    def &&(that: XmlPredicate) : XmlPredicate = p.and(that)

    def ||(that: XmlPredicate) : XmlPredicate = p.or(that)
  }
}