package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlTraverserSyntax {

  implicit class XmlTraverseOps(ns: NodeSeq) {

    def \?(name: String) : ValidatedRes[Option[NodeSeq]] =
      XmlTraverser.immediateChildren(ns, name)

    def \!(name: String) : ValidatedRes[NodeSeq] =
      XmlTraverser.mandatoryImmediateChildren(ns, name)


    def \\?(name: String) : ValidatedRes[Option[NodeSeq]] =
      XmlTraverser.children(ns, name)

    def \\!(name: String) : ValidatedRes[NodeSeq] =
      XmlTraverser.mandatoryChildren(ns, name)


    def \@?(key: String): ValidatedRes[Option[String]] =
      XmlTraverser.attr(ns, key)

    def \@!(key: String): ValidatedRes[String] =
      XmlTraverser.mandatoryAttr(ns, key)


    def content: ValidatedRes[String] =
      XmlTraverser.content(ns)
  }
}
