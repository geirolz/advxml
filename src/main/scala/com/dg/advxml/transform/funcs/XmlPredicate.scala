package com.dg.advxml.transform.funcs

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
trait XmlPredicate extends (NodeSeq => Boolean){

  def and(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) && that(xml)

  def or(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) || that(xml)
}
