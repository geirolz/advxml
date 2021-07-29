package advxml.core.transform

import cats.MonadThrow

import scala.xml.NodeSeq

sealed trait XmlModifier {
  private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadThrow[F]): F[NodeSeq]
}

trait FinalXmlModifier extends XmlModifier
trait ComposableXmlModifier extends XmlModifier
