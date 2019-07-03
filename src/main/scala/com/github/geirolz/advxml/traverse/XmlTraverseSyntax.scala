package com.github.geirolz.advxml.traverse

import scala.util.Try
import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlTraverseSyntax {

  implicit class XmlTraverseOps(ns: NodeSeq) {

    def \?(name: String) : Option[NodeSeq] = XmlTraverser.immediateChildren(ns, name)

    def \!(name: String) : Try[NodeSeq] = XmlTraverser.mandatoryImmediateChildren(ns, name)


    def \\?(name: String) : Option[NodeSeq] =  XmlTraverser.children(ns, name)

    def \\!(name: String) : Try[NodeSeq] = XmlTraverser.mandatoryChildren(ns, name)


    def \@?(key: String): Option[String] = XmlTraverser.attr(ns, key)

    def \@!(key: String): Try[String] = XmlTraverser.mandatoryAttr(ns, key)


    def content: Try[String] = XmlTraverser.content(ns)
  }
}
