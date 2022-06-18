package advxml.xpath.error

import cats.parse.Parser
import eu.cdevreeze.xpathparser.ast.XPathElem

sealed trait Error
final case class ParsingError(err: Parser.Error) extends Error {
  override def toString: String =
    s"Parsing failed at ${err.failedAtOffset}: ${err.expected.toList.mkString}"
}
final case class NotSupportedConstruction(feature: XPathElem) extends Error
