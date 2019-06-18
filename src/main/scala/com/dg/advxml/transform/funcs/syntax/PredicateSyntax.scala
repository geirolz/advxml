package com.dg.advxml.transform.funcs.syntax

import com.dg.advxml.transform.funcs.XmlPredicate

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private [funcs] trait PredicateSyntax {

  implicit class XmlPredicateOps(p: XmlPredicate){

    def &&(that: XmlPredicate) : XmlPredicate = p.and(that)

    def ||(that: XmlPredicate) : XmlPredicate = p.or(that)
  }
}
