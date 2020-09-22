package advxml.syntax

import advxml.core.XmlTraverser
import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser._
import cats.{Applicative, FlatMap}

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
  implicit class XmlTraverserCommonFloatOps(ns: NodeSeq) {

    def atIndexF[F[_]: XmlTraverser](idx: Int): F[NodeSeq] =
      XmlTraverser[F].atIndexF(ns, idx)

    def headF[F[_]: XmlTraverser]: F[NodeSeq] =
      XmlTraverser[F].headF(ns)

    def lastF[F[_]: XmlTraverser]: F[NodeSeq] =
      XmlTraverser[F].lastF(ns)

    def mandatoryTraverser[F[_]]: XmlTraverserMandatoryFloatOpsForId =
      new XmlTraverserMandatoryFloatOpsForId(ns)

    def optionalTraverser[F[_]]: XmlTraverserOptionalFloatOpsForId =
      new XmlTraverserOptionalFloatOpsForId(ns)
  }

  implicit class XmlTraverserMandatoryFloatOpsForId(ns: NodeSeq) {

    def \![F[_]: XmlMandatoryTraverser](q: String): F[NodeSeq] =
      XmlTraverser[F].immediateChildren(ns, q)

    def \\![F[_]: XmlMandatoryTraverser](q: String): F[NodeSeq] =
      XmlTraverser[F].children(ns, q)

    def \@![F[_]: XmlMandatoryTraverser](q: String): F[String] =
      XmlTraverser[F].attr(ns, q)

    def ![F[_]: XmlMandatoryTraverser]: F[String] =
      XmlTraverser[F].text(ns)

    def |!|[F[_]: XmlMandatoryTraverser]: F[String] =
      XmlTraverser[F].trimmedText(ns)

    def \!*[F[_]: Applicative: FlatMap: XmlMandatoryTraverser]: XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](ns)

    def \\!*[F[_]: Applicative: FlatMap: XmlMandatoryTraverser]: XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](ns)
  }

  implicit class XmlTraverserOptionalFloatOpsForId(ns: NodeSeq) {

    def \?[F[_]: XmlOptionalTraverser](q: String): F[NodeSeq] =
      XmlTraverser[F].immediateChildren(ns, q)

    def \\?[F[_]: XmlOptionalTraverser](q: String): F[NodeSeq] =
      XmlTraverser[F].children(ns, q)

    def \@?[F[_]: XmlOptionalTraverser](q: String): F[String] =
      XmlTraverser[F].attr(ns, q)

    def ?[F[_]: XmlOptionalTraverser]: F[String] =
      XmlTraverser[F].text(ns)

    def |?|[F[_]: XmlOptionalTraverser]: F[String] =
      XmlTraverser[F].trimmedText(ns)

    def \?*[F[_]: Applicative: FlatMap: XmlOptionalTraverser]: XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](ns)

    def \\?*[F[_]: Applicative: FlatMap: XmlOptionalTraverser]: XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](ns)
  }

  //######################################## FIXED ########################################
  implicit class XmlTraverserCommonFixedOps[F[_]: FlatMap: XmlTraverser](ns: F[NodeSeq]) {

    def atIndex(idx: Int): F[NodeSeq] =
      ns.flatMap(XmlTraverser[F].atIndexF(_, idx))

    def head: F[NodeSeq] =
      ns.flatMap(XmlTraverser[F].headF)

    def last: F[NodeSeq] =
      ns.flatMap(XmlTraverser[F].lastF)

    def mandatoryTraverser(implicit t: XmlMandatoryTraverser[F]): XmlTraverserMandatoryFixedOps[F] =
      new XmlTraverserMandatoryFixedOps[F](ns)

    def optionalTraverser(implicit t: XmlOptionalTraverser[F]): XmlTraverserOptionalFixedOps[F] =
      new XmlTraverserOptionalFixedOps[F](ns)

  }

  implicit class XmlTraverserMandatoryFixedOps[F[_]: FlatMap: XmlMandatoryTraverser](fa: F[NodeSeq]) {

    def \!(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser[F].immediateChildren(_, q))

    def \\!(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser[F].children(_, q))

    def \@!(q: String): F[String] =
      fa.flatMap(XmlTraverser[F].attr(_, q))

    def ! : F[String] =
      fa.flatMap(XmlTraverser[F].text(_))

    def |!| : F[String] =
      fa.flatMap(XmlTraverser[F].trimmedText(_))

    def \!* : XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](fa)

    def \\!* : XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](fa)
  }

  implicit class XmlTraverserOptionalFixedOps[F[_]: FlatMap: XmlOptionalTraverser](fa: F[NodeSeq]) {

    def \?(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser[F].immediateChildren(_, q))

    def \\?(q: String): F[NodeSeq] =
      fa.flatMap(XmlTraverser[F].children(_, q))

    def \@?(q: String): F[String] =
      fa.flatMap(XmlTraverser[F].attr(_, q))

    def ? : F[String] =
      fa.flatMap(XmlTraverser[F].text(_))

    def |?| : F[String] =
      fa.flatMap(XmlTraverser[F].trimmedText(_))

    def \?* : XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](fa)

    def \\?* : XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](fa)
  }

  //######################################## DYNAMIC ########################################
  implicit class XmlDynamicTraverserMandatoryFixedOps[F[_]: FlatMap: XmlMandatoryTraverser, T <: XmlDynamicTraverser[
    F,
    T
  ]](fa: T) {
    def \!* : XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](fa.get)

    def \\!* : XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](fa.get)
  }

  implicit class XmlDynamicTraverserOptionalFixedOps[F[_]: FlatMap: XmlOptionalTraverser, T <: XmlDynamicTraverser[
    F,
    T
  ]](fa: T) {

    def \?* : XmlImmediateDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.immediate[F](fa.get)

    def \\?* : XmlDeepDynamicTraverser[F] =
      advxml.instances.traverse.dynamic.deep[F](fa.get)
  }

  implicit def xmlDynamicTraverserToF[F[_], T <: XmlDynamicTraverser[F, T]](dynamic: T): F[NodeSeq] = dynamic.get
}

private[syntax] trait XmlTraverserSyntaxSpecified[F[_], G[_]] extends XmlTraverserSyntax {

  implicit class XmlTraverserMandatoryFixedOpsForId[F1[_] >: F[_]: Applicative: FlatMap: XmlMandatoryTraverser](
    ns: NodeSeq
  )(
    implicit F: MonadEx[F],
    T: XmlTraverser[F]
  ) extends XmlTraverserMandatoryFixedOps[F1](Applicative[F1].pure(ns))

  implicit class XmlTraverserOptionalFixedOpsForId[G1[_] >: G[_]: Applicative: FlatMap: XmlOptionalTraverser](
    ns: NodeSeq
  ) extends XmlTraverserOptionalFixedOps[G1](Applicative[G1].pure(ns))
}
