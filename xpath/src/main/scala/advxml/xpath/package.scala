package advxml

import advxml.transform.XmlZoom
import advxml.xpath.error.XPathError
import cats.data.{Validated, ValidatedNel}
import eu.cdevreeze.xpathparser.ast.{EQName, XPathExpr}
import eu.cdevreeze.xpathparser.parse.XPathParser

package object xpath {
  implicit class XmlZoomCompanionExt(comp: XmlZoom.type) {
    def fromXPath(xPath: String): ValidatedNel[XPathError, XmlZoom] = {
      Validated
        .fromEither(XPathParser.xpathExpr.parseAll(xPath).left.map(XPathError.ParsingError(_): XPathError))
        .toValidatedNel
        .andThen(fromXPathExpr)
    }

    def fromXPathExpr(xPathExpr: XPathExpr): ValidatedNel[XPathError, XmlZoom] =
      XmlZoomBuilder.modifyZoom(xPathExpr).map(_(XmlZoom.root))

  }

  private[xpath] object EmptySeq {
    def unapply(io: Iterable[?]): Boolean = io.isEmpty
  }

  private[xpath] object UnSeq {
    def unapplySeq(io: Iterable[?]): Option[Seq[?]] = Some(io.toSeq)
  }

  private[xpath] object EQNameEx {
    def unapply(eqn: EQName): Some[String] =
      eqn match {
        case EQName.QName(qname)            => Some(qname.localPart)
        case EQName.URIQualifiedName(ename) => Some(ename.localPart)
      }
  }
}
