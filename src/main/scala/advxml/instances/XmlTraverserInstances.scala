package advxml.instances

import advxml.core.XmlTraverser
import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser.{
  XmlDeepDynamicTraverser,
  XmlImmediateDynamicTraverser,
  XmlMandatoryTraverser,
  XmlOptionalTraverser,
  XmlTraverser
}
import advxml.core.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}
import advxml.core.utils.OptErrorHandler
import advxml.core.utils.OptErrorHandler.OptErrorHandler
import cats.{Alternative, Applicative, FlatMap}

import scala.xml.NodeSeq

private[instances] trait AllXmlTraverserInstances extends XmlTraverserInstances with DynamicXmlTraverserInstances

private[instances] trait XmlTraverserInstances {

  import cats.syntax.functor._

  private class XmlTraverserImpl[F[_]](errHandler: OptErrorHandler[F])(implicit F: Applicative[F])
      extends XmlTraverser[F] {

    override def immediateChildren(ns: NodeSeq, q: String): F[NodeSeq] =
      errHandler(XmlMissingNodeException(q, ns))(ns \ q match {
        case value if value.isEmpty => None
        case value                  => Some(value)
      })

    override def children(ns: NodeSeq, q: String): F[NodeSeq] =
      errHandler(XmlMissingNodeException(q, ns))(ns \\ q match {
        case value if value.isEmpty => None
        case value                  => Some(value)
      })

    override def attr(ns: NodeSeq, q: String): F[String] =
      errHandler(XmlMissingAttributeException(q, ns))(ns \@ q match {
        case value if value.isEmpty => None
        case value                  => Some(value)
      })

    override def text(ns: NodeSeq): F[String] =
      errHandler(XmlMissingTextException(ns))(
        ns.text match {
          case value if value.isEmpty => None
          case value                  => Some(value)
        }
      )

    override def trimmedText(ns: NodeSeq): F[String] =
      text(ns).map(_.trim)

    override def atIndexF(ns: NodeSeq, idx: Int): F[NodeSeq] =
      errHandler(new IndexOutOfBoundsException("" + idx))(ns.lift(idx))

    override def headF(ns: NodeSeq): F[NodeSeq] =
      errHandler(XmlMissingNodeException("head", ns))(ns.headOption)

    override def lastF(ns: NodeSeq): F[NodeSeq] =
      errHandler(XmlMissingNodeException("last", ns))(ns.lastOption)

    override def tailF(ns: NodeSeq): F[NodeSeq] =
      F.pure(ns.tail)

    override def findF(ns: NodeSeq, p: NodeSeq => Boolean): F[NodeSeq] =
      errHandler(new RuntimeException("Cannot find an Node that satisfies the predicate."))(ns.find(p))

    override def filterF(ns: NodeSeq, p: NodeSeq => Boolean): F[NodeSeq] =
      F.pure(ns.filter(p))
  }

  implicit def mandatory[F[_]](implicit F: MonadEx[F]): XmlMandatoryTraverser[F] = {
    new XmlTraverserImpl[F](OptErrorHandler.optErrorHandlerForMonadEx[F]) with XmlMandatoryTraverser[F]
  }

  implicit def optional[F[_]](implicit F: Alternative[F]): XmlOptionalTraverser[F] = {
    new XmlTraverserImpl[F](OptErrorHandler.optErrorHandlerForAlternative[F]) with XmlOptionalTraverser[F]
  }
}

private[instances] trait DynamicXmlTraverserInstances {

  import cats.syntax.all._

  trait immediate {

    def apply[F[_]: Applicative: FlatMap: XmlTraverser](v: NodeSeq): XmlImmediateDynamicTraverser[F] =
      apply(Applicative[F].pure(v))

    def apply[F[_]: FlatMap: XmlTraverser](v: F[NodeSeq]): XmlImmediateDynamicTraverser[F] = {

      case class XmlImmediateDynamicTraverserImpl(value: F[NodeSeq]) extends XmlImmediateDynamicTraverser[F] {

        override def get: F[NodeSeq] = value

        override def selectDynamic(q: String): XmlImmediateDynamicTraverser[F] =
          copy(value.flatMap(v => XmlTraverser[F].immediateChildren(v, q)))

        override def applyDynamic(q: String)(idx: Int): XmlImmediateDynamicTraverser[F] =
          copy(selectDynamic(q).get.flatMap(XmlTraverser[F].atIndexF(_, idx)))
      }

      XmlImmediateDynamicTraverserImpl(v)
    }
  }

  trait deep {

    def apply[F[_]: Applicative: FlatMap: XmlTraverser](v: NodeSeq): XmlDeepDynamicTraverser[F] =
      apply(Applicative[F].pure(v))

    def apply[F[_]: FlatMap: XmlTraverser](v: F[NodeSeq]): XmlDeepDynamicTraverser[F] = {

      case class XmlDeepDynamicTraverserImpl(value: F[NodeSeq]) extends XmlDeepDynamicTraverser[F] {

        override def get: F[NodeSeq] = value

        override def selectDynamic(q: String): XmlDeepDynamicTraverser[F] =
          copy(value.flatMap(v => XmlTraverser[F].children(v, q)))

        override def applyDynamic(q: String)(idx: Int): XmlDeepDynamicTraverser[F] =
          copy(selectDynamic(q).get.flatMap(XmlTraverser[F].atIndexF(_, idx)))
      }

      XmlDeepDynamicTraverserImpl(v)
    }
  }
}
