package com.github.geirolz.advxml.traverse

import cats.data.Validated.Valid
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTraverserSyntax {

  implicit class XmlTraverseNodeSeqOps(ns: NodeSeq) {

    def \?(name: String): ValidatedRes[Option[NodeSeq]] =
      XmlTraverser.optional.immediateChildren(ns, name)

    def \!(name: String): ValidatedRes[NodeSeq] =
      XmlTraverser.mandatory.immediateChildren(ns, name)

    def \\?(name: String): ValidatedRes[Option[NodeSeq]] =
      XmlTraverser.optional.children(ns, name)

    def \\!(name: String): ValidatedRes[NodeSeq] =
      XmlTraverser.mandatory.children(ns, name)

    def \@?(key: String): ValidatedRes[Option[String]] =
      XmlTraverser.optional.attr(ns, key)

    def \@!(key: String): ValidatedRes[String] =
      XmlTraverser.mandatory.attr(ns, key)

    def ? : ValidatedRes[Option[String]] =
      XmlTraverser.optional.text(ns)

    def ! : ValidatedRes[String] =
      XmlTraverser.mandatory.text(ns)
  }

  implicit class XmlTraverseMandatoryOps(v: ValidatedRes[NodeSeq]) {

    def \?(name: String): ValidatedRes[Option[NodeSeq]] =
      v.andThen(_ \? name)

    def \!(name: String): ValidatedRes[NodeSeq] =
      v.andThen(_ \! name)

    def \\?(name: String): ValidatedRes[Option[NodeSeq]] =
      v.andThen(_ \\? name)

    def \\!(name: String): ValidatedRes[NodeSeq] =
      v.andThen(_ \\! name)

    def \@?(key: String): ValidatedRes[Option[String]] =
      v.andThen(_ \@? key)

    def \@!(key: String): ValidatedRes[String] =
      v.andThen(_ \@! key)

    def ? : ValidatedRes[Option[String]] =
      v.andThen(_.?)

    def ! : ValidatedRes[String] =
      v.andThen(_.!)
  }

  implicit class XmlTraverseOptionOps(v: ValidatedRes[Option[NodeSeq]]) {

    def \?(name: String): ValidatedRes[Option[NodeSeq]] =
      v.andThen(ifSome(_ \? name))

    def \\?(name: String): ValidatedRes[Option[NodeSeq]] =
      v.andThen(ifSome(_ \\? name))

    def \@?(key: String): ValidatedRes[Option[String]] =
      v.andThen(ifSome(_ \@? key))

    def ? : ValidatedRes[Option[String]] =
      v.andThen(ifSome(_.?))

    private def ifSome[A, B](f: A => ValidatedRes[Option[B]]): Option[A] => ValidatedRes[Option[B]] = {
      case Some(ns) => f(ns)
      case None     => Valid(None)
    }
  }
}
