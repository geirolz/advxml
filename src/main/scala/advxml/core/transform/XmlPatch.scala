package advxml.core.transform

import scala.xml.NodeSeq

case class XmlPatch(original: NodeSeq, updated: NodeSeq) {

  def zipWithUpdated: Map[Option[NodeSeq], Option[NodeSeq]] = {
    import cats.implicits._
    original.toList.padZip(updated.toList).toMap
  }
}

object XmlPatch {

  def pure(original: NodeSeq): XmlPatch = XmlPatch(original, original)

  def apply[T <: NodeSeq](original: T)(f: T => T): XmlPatch = XmlPatch(original, f(original))
}
