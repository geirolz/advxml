package advxml.core.transform.exceptions

import advxml.core.transform.actions.XmlZoom.XmlZoom

import scala.xml.NodeSeq

case class EmptyTargetException(root: NodeSeq, zoom: XmlZoom)
    extends RuntimeException(
      "Empty target, root doesn't contains any element in the location specified by the zoom instance."
    )
