package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.Validation.ValidationRes

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlTraverserSyntax {

  implicit class XmlTraverseOps(ns: NodeSeq) {

    def \?(name: String) : ValidationRes[Option[NodeSeq]] =
      XmlTraverser.immediateChildren(ns, name)

    def \!(name: String) : ValidationRes[NodeSeq] =
      XmlTraverser.mandatoryImmediateChildren(ns, name)


    def \\?(name: String) : ValidationRes[Option[NodeSeq]] =
      XmlTraverser.children(ns, name)

    def \\!(name: String) : ValidationRes[NodeSeq] =
      XmlTraverser.mandatoryChildren(ns, name)


    def \@?(key: String): ValidationRes[Option[String]] =
      XmlTraverser.attr(ns, key)

    def \@!(key: String): ValidationRes[String] =
      XmlTraverser.mandatoryAttr(ns, key)


    def content: ValidationRes[String] =
      XmlTraverser.content(ns)
  }
}
