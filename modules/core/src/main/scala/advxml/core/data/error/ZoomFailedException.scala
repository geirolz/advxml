package advxml.core.data.error

import advxml.core.transform.XmlZoom.ZoomAction
import advxml.core.transform.BindedXmlZoom
import advxml.core.utils.XmlUtils

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
