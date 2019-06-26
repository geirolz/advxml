package com.dg.advxml.transform

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 26/06/2019.
  *
  * @author geirolad
  */
package object actions {

  type XmlPredicate = NodeSeq => Boolean
  type XmlZoom = NodeSeq => NodeSeq

  implicit class XmlPredicateOps(p: XmlPredicate) {

    def and(that: XmlPredicate): XmlPredicate = xml => p(xml) && that(xml)

    def or(that: XmlPredicate): XmlPredicate = xml => p(xml) || that(xml)
  }

  object XmlZoom{
    def apply(f: NodeSeq => NodeSeq): XmlZoom = f(_)
  }
}
