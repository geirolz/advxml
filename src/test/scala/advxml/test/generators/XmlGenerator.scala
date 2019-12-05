package advxml.test.generators

import org.scalacheck.Gen

import scala.xml._

/**
  * Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlGenerator {

  lazy val genStr: Int => Gen[String] = (n: Int) => Gen.listOfN(n, Gen.alphaChar).map(_.mkString)
  lazy val attrsGenerator: Gen[Map[String, String]] = {
    for {
      n <- Gen.choose(1, 2)
      kvGen = for {
        key   <- genStr(5)
        value <- genStr(5)
      } yield (key, value)
      map <- Gen.mapOfN(n, kvGen)
    } yield map
  }
  lazy val xmlNodeGenerator: Gen[BasicXmlNode] = for {
    nodeName <- genStr(5)

    attrs <- for {
      hasAttrs <- Gen.frequency((80, true), (20, false))
      attrs    <- if (hasAttrs) attrsGenerator else Gen.const(Map.empty[String, String])
    } yield attrs

    children <- for {
      hasChildren <- Gen.frequency((80, true), (20, false))
      children <- if (hasChildren)
        Gen.resize(1, Gen.nonEmptyListOf(xmlNodeGenerator))
      else
        Gen.const(List.empty)
    } yield children
  } yield BasicXmlNode(nodeName, attrs, children)
  lazy val xmlNodeSelectorGenerator: Node => Gen[NodeSeq] = elem => Gen.oneOf(elem.descendant).filter(_ != elem)

  def toNode(node: BasicXmlNode): Node = {
    val seed: MetaData = Null
    val meta: MetaData = node.attrs.toList.foldLeft(seed) {
      case (acc, (s1, s2)) =>
        new UnprefixedAttribute(
          key = s1,
          value = s2,
          next = acc
        )
    }

    Elem(
      prefix = null,
      label = node.name,
      attributes = meta,
      scope = TopScope,
      minimizeEmpty = false,
      child = node.children.map(toNode): _*
    )
  }

  case class BasicXmlNode(name: String, attrs: Map[String, String], children: Seq[BasicXmlNode]) {
    def toNode: Node = XmlGenerator.toNode(this)
  }
}
