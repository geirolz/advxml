package advxml.data.error

import advxml.transform.XmlZoom.ZoomAction
import advxml.transform.BindedXmlZoom
import advxml.utils.XmlUtils

case class ZoomFailedException(
  bindedZoom: BindedXmlZoom,
  failingAction: ZoomAction,
  pathLog: String = ""
) extends RuntimeException(
      "## Zoom Failed\n" +
        s"- Zoom: ${ZoomAction.asStringPath(bindedZoom.actions)}\n" +
        s"- Failed Action: $failingAction\n" +
        s"- Success Path: $pathLog\n" +
        s"- Document: \n${XmlUtils.prettyPrint(bindedZoom.document)}\n"
    )
