package advxml

import advxml.transform.XmlZoom
import advxml.xpath.error.{Error, ParsingError}
import cats.data.{Validated, ValidatedNel}
import eu.cdevreeze.xpathparser.ast.{EQName, XPathExpr}
import eu.cdevreeze.xpathparser.parse.XPathParser

package object xpath {
  implicit class XmlZoomCompanionExt(comp: XmlZoom.type) {
    def fromXPath(xPath: String): ValidatedNel[Error, XmlZoom] = {
      Validated
        .fromEither(XPathParser.xpathExpr.parseAll(xPath).left.map(ParsingError(_): Error))
        .toValidatedNel
        .andThen(fromXPathExpr)
    }

    def fromXPathExpr(xPathExpr: XPathExpr): ValidatedNel[Error, XmlZoom] =
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
