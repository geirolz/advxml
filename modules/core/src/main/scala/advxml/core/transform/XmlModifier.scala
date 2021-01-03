package advxml.core.transform

import advxml.core.MonadEx

import scala.xml.NodeSeq

sealed trait XmlModifier {
  private[advxml] def apply[F[_]](ns: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq]
}

trait FinalXmlModifier extends XmlModifier
trait ComposableXmlModifier extends XmlModifier
