package advxml.syntax

import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser
import cats.{Alternative, FlatMap, Monad}

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private[syntax] trait XmlTraverserSyntax {

  import cats.syntax.all._

  //######################################## FLOAT ########################################
  implicit class XmlTraverserMandatoryFloatOpsForId(ns: NodeSeq) {
    def \![F[_]: MonadEx](q: String): F[NodeSeq] =
      XmlTraverser.mandatory[F].immediateChildren(ns, q)

    def \\![F[_]: MonadEx](q: String): F[NodeSeq] =
      XmlTraverser.mandatory[F].children(ns, q)

    def \@![F[_]: MonadEx](q: String): F[String] =
      XmlTraverser.mandatory[F].attr(ns, q)

    def ![F[_]: MonadEx]: F[String] =
      XmlTraverser.mandatory[F].text(ns)

    def |!|[F[_]: MonadEx]: F[String] =
      XmlTraverser.mandatory[F].trimmedText(ns)
  }

  implicit class XmlTraverserOptionalFloatOpsForId(ns: NodeSeq) {
    def \?[F[_]: Alternative](q: String): F[NodeSeq] =
      XmlTraverser.optional[F].immediateChildren(ns, q)

    def \\?[F[_]: Alternative](q: String): F[NodeSeq] =
      XmlTraverser.optional[F].children(ns, q)

    def \@?[F[_]: Alternative](q: String): F[String] =
      XmlTraverser.optional[F].attr(ns, q)

    def ?[F[_]: Alternative]: F[String] =
      XmlTraverser.optional[F].text(ns)

    def |?|[F[_]: Alternative]: F[String] =
      XmlTraverser.optional[F].trimmedText(ns)
  }

  //######################################## FIXED ########################################
  implicit class XmlTraverserMandatoryFixedOps[F[_]: MonadEx](fa: F[NodeSeq]) {

    def \!(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser.mandatory[F].immediateChildren(_, q))

    def \\!(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser.mandatory[F].children(_, q))

    def \@!(q: String): F[String] =
      fa.flatMap(XmlTraverser.mandatory[F].attr(_, q))

    def ! : F[String] =
      fa.flatMap(XmlTraverser.mandatory[F].text(_))

    def |!| : F[String] =
      fa.flatMap(XmlTraverser.mandatory[F].trimmedText(_))
  }

  implicit class XmlTraverserOptionalFixedOps[F[_]: Alternative: FlatMap](fa: F[NodeSeq]) {

    def \?(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser.optional[F].immediateChildren(_, q))

    def \\?(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser.optional[F].children(_, q))

    def \@?(q: String): F[String] =
      fa.flatMap(XmlTraverser.optional[F].attr(_, q))

    def ? : F[String] =
      fa.flatMap(XmlTraverser.optional[F].text(_))

    def |?| : F[String] =
      fa.flatMap(XmlTraverser.optional[F].trimmedText(_))
  }
}

private[syntax] trait XmlTraverserSyntaxSpecified[F[_], G[_]] extends XmlTraverserSyntax {

  implicit class XmlTraverserMandatoryFixedOpsForId(ns: NodeSeq)(implicit F: MonadEx[F])
      extends XmlTraverserMandatoryFixedOps[F](F.pure(ns))

  implicit class XmlTraverserOptionalFixedOpsForId(ns: NodeSeq)(implicit M: Monad[G], A: Alternative[G])
      extends XmlTraverserOptionalFixedOps[G](M.pure(ns))
}
