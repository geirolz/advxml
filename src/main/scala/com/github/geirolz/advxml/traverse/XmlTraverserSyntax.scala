package com.github.geirolz.advxml.traverse

import cats.{Alternative, Monad}
import com.github.geirolz.advxml.transform.actions.MonadEx

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTraverserSyntax {

  implicit class XmlTraverseNodeSeqOps(ns: NodeSeq) {

    def \?[F[_]: Alternative](name: String): F[NodeSeq] =
      XmlTraverser.optional.immediateChildren(ns, name)

    def \\?[F[_]: Alternative](name: String): F[NodeSeq] =
      XmlTraverser.optional.children(ns, name)

    def \@?[F[_]: Alternative](key: String): F[String] =
      XmlTraverser.optional.attr(ns, key)

    def ?[F[_]: Alternative]: F[String] =
      XmlTraverser.optional.text(ns)

    def \![F[_]: MonadEx](name: String): F[NodeSeq] =
      XmlTraverser.mandatory.immediateChildren(ns, name)

    def \\![F[_]: MonadEx](name: String): F[NodeSeq] =
      XmlTraverser.mandatory.children(ns, name)

    def \@![F[_]: MonadEx](key: String): F[String] =
      XmlTraverser.mandatory.attr(ns, key)

    def ![F[_]: MonadEx]: F[String] =
      XmlTraverser.mandatory.text(ns)
  }

  implicit class XmlTraverseMandatoryOps[F[_]](v: F[NodeSeq])(implicit F: MonadEx[F]) {

    def \!(name: String): F[NodeSeq] =
      F.flatMap(v)(_ \! name)

    def \\!(name: String): F[NodeSeq] =
      F.flatMap(v)(_ \\! name)

    def \@!(key: String): F[String] =
      F.flatMap(v)(_ \@! key)

    def ! : F[String] =
      F.flatMap(v)(_.!)
  }

  implicit class XmlTraverseOptionOps[F[_]: Monad](v: F[NodeSeq])(implicit F: Alternative[F]) {

    def \?(name: String): F[NodeSeq] =
      Monad[F].flatten(F.fmap(v)(_ \? name))

    def \\?(name: String): F[NodeSeq] =
      Monad[F].flatten(F.fmap(v)(_ \\? name))

    def \@?(key: String): F[String] =
      Monad[F].flatten(F.fmap(v)(_ \@? key))

    def ? : F[String] =
      Monad[F].flatten(F.fmap(v)(_.?))
  }
}
