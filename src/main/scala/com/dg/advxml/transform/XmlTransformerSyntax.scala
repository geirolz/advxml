package com.dg.advxml.transform

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
    def ==>(action: XmlAction): XmlRule = r.withAction(action)
  }
}

private [transform] sealed trait ActionsSyntax {

  implicit class XmlActionOps(a: XmlAction) {
    def ++(that: XmlAction) : XmlAction = a.andThen(that)
  }
}

private [transform] sealed trait ZoomSyntax{

  implicit class XmlZoomOps(z: XmlZoom){
    def \(that: XmlZoom) : XmlZoom = z.andThen(that)
  }
}

private [transform] sealed trait PredicateSyntax {

  implicit class XmlPredicateOps(p: XmlPredicate){

    def &&(that: XmlPredicate) : XmlPredicate = p.and(that)

    def ||(that: XmlPredicate) : XmlPredicate = p.or(that)
  }
}