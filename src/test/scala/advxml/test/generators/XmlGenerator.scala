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

  def genStr(size: Int): Gen[String] = Gen.listOfN(size, Gen.alphaChar).map(_.mkString)

  def attrsGenerator(maxSize: Int, nameMaxSize: Int): Gen[Map[String, String]] = {
    for {
      n <- Gen.choose(1, maxSize)
      kvGen = for {
        key   <- genStr(nameMaxSize)
        value <- genStr(nameMaxSize)
      } yield (key, value)
      map <- Gen.mapOfN(n, kvGen)
    } yield map
  }
  lazy val xmlNodeSelectorGenerator: Node => Gen[NodeSeq] = elem => Gen.oneOf(elem.descendant).filter(_ != elem)

  def xmlNodeGenerator(maxLevel: Int, level: Int = 0): Gen[BasicXmlNode] =
    for {
      nodeName <- genStr(10)

      attrs <- for {
        hasAttrs <- Gen.frequency((80, true), (20, false))
        attrs <- if (hasAttrs)
          attrsGenerator(maxSize = 50, nameMaxSize = 5)
        else
          Gen.const(Map.empty[String, String])
      } yield attrs

      children <- for {
        hasChildren <- Gen.frequency((70, true), (30, false))
        children <- if (level < maxLevel && hasChildren)
          Gen.lzy(Gen.resize(5, Gen.nonEmptyListOf(xmlNodeGenerator(maxLevel, level + 1))))
        else
          Gen.const(List.empty)
      } yield children

    } yield BasicXmlNode(nodeName, attrs, children)

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
