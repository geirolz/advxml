package com.github.geirolz.advxml.traverse

import cats.{Alternative, Monad}
import com.github.geirolz.advxml.transform.actions.MonadEx

import scala.util.Try
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTraverserSyntax extends XmlTraverserAbstractSyntax {

  import cats.instances.option._
  import cats.instances.try_._

  implicit class XmlTraverseNodeSeqTryOptionOps(target: NodeSeq) {
    def \!(q: String): Try[NodeSeq] =
      XmlTraverser.mandatory.immediateChildren(target, q)

    def \\!(q: String): Try[NodeSeq] =
      XmlTraverser.mandatory.children(target, q)

    def \@!(q: String): Try[String] =
      XmlTraverser.mandatory.attr(target, q)

    def ! : Try[String] =
      XmlTraverser.mandatory.text(target)

    def \?(q: String): Option[NodeSeq] =
      XmlTraverser.optional.immediateChildren(target, q)

    def \\?(q: String): Option[NodeSeq] =
      XmlTraverser.optional.children(target, q)

    def \@?(q: String): Option[String] =
      XmlTraverser.optional.attr(target, q)

    def ? : Option[String] =
      XmlTraverser.optional.text(target)
  }

  implicit class XmlTraverseTryOps(target: Try[NodeSeq]) extends XmlTraverseMonadExOps[Try](target)

  implicit class XmlTraverseOptionOps(target: Option[NodeSeq]) extends XmlTraverseAlternativeOps[Option](target)

}

private[advxml] trait XmlTraverserAbstractSyntax {

  implicit class XmlTraverseNodeSeqOps(target: NodeSeq) {

    def \![F[_]: MonadEx](q: String): F[NodeSeq] =
      XmlTraverser.mandatory.immediateChildren(target, q)

    def \\![F[_]: MonadEx](q: String): F[NodeSeq] =
      XmlTraverser.mandatory.children(target, q)

    def \@![F[_]: MonadEx](q: String): F[String] =
      XmlTraverser.mandatory.attr(target, q)

    def ![F[_]: MonadEx]: F[String] =
      XmlTraverser.mandatory.text(target)

    def \?[F[_]: Alternative](q: String): F[NodeSeq] =
      XmlTraverser.optional.immediateChildren(target, q)

    def \\?[F[_]: Alternative](q: String): F[NodeSeq] =
      XmlTraverser.optional.children(target, q)

    def \@?[F[_]: Alternative](q: String): F[String] =
      XmlTraverser.optional.attr(target, q)

    def ?[F[_]: Alternative]: F[String] =
      XmlTraverser.optional.text(target)
  }

  implicit class XmlTraverseMonadExOps[F[_]](targetF: F[NodeSeq])(implicit F: MonadEx[F]) {

    def \!(q: String): F[NodeSeq] =
      F.flatMap(targetF)(_ \! q)

    def \\!(q: String): F[NodeSeq] =
      F.flatMap(targetF)(_ \\! q)

    def \@!(q: String): F[String] =
      F.flatMap(targetF)(_ \@! q)

    def ! : F[String] =
      F.flatMap(targetF)(_.!)
  }

  implicit class XmlTraverseAlternativeOps[F[_]: Monad](targetF: F[NodeSeq])(implicit F: Alternative[F]) {

    def \?(q: String): F[NodeSeq] =
      Monad[F].flatten(F.fmap(targetF)(_ \? q))

    def \\?(q: String): F[NodeSeq] =
      Monad[F].flatten(F.fmap(targetF)(_ \\? q))

    def \@?(q: String): F[String] =
      Monad[F].flatten(F.fmap(targetF)(_ \@? q))

    def ? : F[String] =
      Monad[F].flatten(F.fmap(targetF)(_.?))
  }
}
