package advxml.core.transform.exceptions

import advxml.core.transform.XmlZoom.ZoomAction
import advxml.core.transform.XmlZoom
import advxml.core.utils.XmlUtils

import scala.xml.NodeSeq

case class ZoomFailedException(wholeDocument: NodeSeq, zoom: XmlZoom, failingAction: ZoomAction, pathLog: String = "")
    extends RuntimeException(
      "Zoom Failed!\n" +
      s"- Action: $failingAction\n" +
      s"- Path: $pathLog\n" +
      s"- Document: \n${XmlUtils.prettyPrint(wholeDocument)}\n"
    )
