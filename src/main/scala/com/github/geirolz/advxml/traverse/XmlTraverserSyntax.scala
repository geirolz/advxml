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
      XmlTraverser.optional.immediateChildren(ns, name)

    def \!(name: String) : ValidatedRes[NodeSeq] =
      XmlTraverser.mandatory.immediateChildren(ns, name)


    def \\?(name: String) : ValidatedRes[Option[NodeSeq]] =
      XmlTraverser.optional.children(ns, name)

    def \\!(name: String) : ValidatedRes[NodeSeq] =
      XmlTraverser.mandatory.children(ns, name)


    def \@?(key: String): ValidatedRes[Option[String]] =
      XmlTraverser.optional.attr(ns, key)

    def \@!(key: String): ValidatedRes[String] =
      XmlTraverser.mandatory.attr(ns, key)


    def ? : ValidatedRes[Option[String]] =
      XmlTraverser.optional.content(ns)

    def ! : ValidatedRes[String] =
      XmlTraverser.mandatory.content(ns)
  }
}
