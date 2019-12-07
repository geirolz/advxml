package advxml.core.transform.actions

import scala.xml.NodeSeq

object XmlZoom {
  type XmlZoom = NodeSeq => NodeSeq
  def apply(f: NodeSeq => NodeSeq): XmlZoom = f(_)
}
