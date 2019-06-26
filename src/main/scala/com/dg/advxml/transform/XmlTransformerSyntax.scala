package com.dg.advxml.transform

import com.dg.advxml.transform.actions.{ComposableXmlModifier, FinalXmlModifier, XmlPredicate, XmlZoom}
import com.dg.advxml.utils.PredicateUtils

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlTransformerSyntax
  extends RuleSyntax
    with ModifiersSyntax
    with ZoomSyntax
    with PredicateSyntax

private [transform] sealed trait RuleSyntax {

  def $(zoom: XmlZoom): PartialXmlRule = PartialXmlRule(zoom)

  implicit class PartialRuleOps(r: PartialXmlRule) {
    def ==>(modifier: FinalXmlModifier): FinalXmlRule = r.withModifier(modifier)
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }

  implicit class ModifierCompatibleOps(r: ComposableXmlRule) {
    def ==>(modifier: ComposableXmlModifier): ComposableXmlRule = r.withModifier(modifier)
  }
}

private [transform] sealed trait ModifiersSyntax {

  implicit class ComposableXmlModifierOps(a: ComposableXmlModifier) {
    def ++(that: ComposableXmlModifier) : ComposableXmlModifier = a.andThen(that)
  }
}

private [transform] sealed trait ZoomSyntax{

  implicit class XmlZoomOps(z: XmlZoom){
    def \(that: XmlZoom) : XmlZoom = z.andThen(that)
  }
}

private [transform] sealed trait PredicateSyntax {

  implicit class XmlPredicateOps(p: XmlPredicate){

    def and(that: XmlPredicate): XmlPredicate = PredicateUtils.and(p, that)

    def or(that: XmlPredicate): XmlPredicate = PredicateUtils.or(p, that)

    def &&(that: XmlPredicate) : XmlPredicate = p.and(that)

    def ||(that: XmlPredicate) : XmlPredicate = p.or(that)
  }
}