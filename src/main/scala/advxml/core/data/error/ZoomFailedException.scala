package advxml.core.data.error

import advxml.core.transform.XmlZoom.ZoomAction
import advxml.core.transform.XmlZoomBinded
import advxml.core.utils.XmlUtils

case class ZoomFailedException(bindedZoom: XmlZoomBinded, failingAction: ZoomAction, pathLog: String = "")
    extends RuntimeException(
      "Zoom Failed!\n" +
      s"- Action: $failingAction\n" +
      s"- Path: $pathLog\n" +
      s"- Document: \n${XmlUtils.prettyPrint(bindedZoom.document)}\n"
    )
