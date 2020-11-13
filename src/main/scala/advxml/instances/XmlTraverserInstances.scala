package advxml.instances

import advxml.core.XmlTraverser
import advxml.core.validate.MonadEx
import advxml.core.XmlTraverser._
import advxml.core.XmlTraverser.exceptions.{
  XmlMissingAttributeException,
  XmlMissingNodeException,
  XmlMissingTextException
}
import advxml.core.XmlPredicate
import advxml.core.utils.{OptErrorHandler, TraverserK}
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

    val childTraverser: TraverserK[NodeSeq, NodeSeq, F] = new TraverserK[NodeSeq, NodeSeq, F] {
      override def atIndex(ns: NodeSeq, idx: Int): F[NodeSeq] =
        errHandler(new IndexOutOfBoundsException("" + idx))(ns.\("_").lift(idx))

      override def head(ns: NodeSeq): F[NodeSeq] =
        errHandler(XmlMissingNodeException("head", ns))(ns.\("_").headOption)

      override def last(ns: NodeSeq): F[NodeSeq] =
        errHandler(XmlMissingNodeException("last", ns))(ns.\("_").lastOption)

      override def tail(ns: NodeSeq): F[NodeSeq] =
        F.pure(ns.\("_").tail)

      override def find(ns: NodeSeq, p: XmlPredicate): F[NodeSeq] =
        errHandler(new RuntimeException("Cannot find an Node that satisfies the predicate."))(ns.\("_").find(p))

      override def filter(ns: NodeSeq, p: XmlPredicate): F[NodeSeq] =
        F.pure(ns.\("_").filter(p))
    }
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
          copy(selectDynamic(q).get.flatMap(XmlTraverser[F].childTraverser.atIndex(_, idx)))
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
          copy(selectDynamic(q).get.flatMap(XmlTraverser[F].childTraverser.atIndex(_, idx)))
      }

      XmlDeepDynamicTraverserImpl(v)
    }
  }
}
