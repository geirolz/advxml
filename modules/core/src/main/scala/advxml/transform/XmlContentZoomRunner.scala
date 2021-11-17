package advxml.transform
import advxml.data.*
import advxml.ApplicativeThrowOrEu
import cats.FlatMap

import scala.xml.{Node, NodeSeq}

case class XmlContentZoomRunner(zoom: BindedXmlZoom, f: NodeSeq => Value)
    extends AsValidable[XmlContentZoomRunner] {

  import cats.syntax.flatMap.*

  def validate(nrule: ValidationRule, nrules: ValidationRule*): XmlContentZoomRunner =
    copy(f = f.andThen((v: Value) => v.validate(nrule, nrules*)))

  def validated: ValidatedNelThrow[String] =
    zoom.run[ValidatedNelThrow].andThen(ns => f(ns).extract[ValidatedNelThrow])

  def extract[F[_]: ApplicativeThrowOrEu: FlatMap]: F[String] =
    zoom.run[F].flatMap(ns => f(ns).extract[F])
}

object XmlContentZoom {

  // =========================== FROM NODESEQ ===========================
  def label(ns: NodeSeq): SimpleValue =
    ns match {
      case node: Node => SimpleValue(node.label)
      case _          => SimpleValue("")
    }

  def attr(ns: NodeSeq, key: String): ValidatedValue =
    SimpleValue(ns \@ key, Some(s"${label(ns).get} /@ $key")).nonEmpty

  def content(ns: NodeSeq): ValidatedValue =
    SimpleValue(ns.text, Some(s"${label(ns).get}.content")).nonEmpty

  // ========================= FROM BINDED ZOOM =========================
  def labelFromBindedZoom(zoom: BindedXmlZoom): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => label(ns))

  def attrFromBindedZoom(zoom: BindedXmlZoom, key: String): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => attr(ns, key))

  def contentFromBindedZoom(zoom: BindedXmlZoom): XmlContentZoomRunner =
    XmlContentZoomRunner(zoom, ns => content(ns))

  // ========================= FROM UNBINDED ZOOM =========================
  def labelFromZoom(zoom: XmlZoom, ns: NodeSeq): XmlContentZoomRunner =
    labelFromBindedZoom(zoom.bind(ns))

  def attrFromZoom(zoom: XmlZoom, ns: NodeSeq, key: String): XmlContentZoomRunner =
    attrFromBindedZoom(zoom.bind(ns), key)

  def contentFromZoom(zoom: XmlZoom, ns: NodeSeq): XmlContentZoomRunner =
    contentFromBindedZoom(zoom.bind(ns))
}
