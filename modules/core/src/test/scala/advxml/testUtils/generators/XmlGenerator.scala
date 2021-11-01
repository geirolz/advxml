package advxml.testUtils.generators

import advxml.core.data.{AttributeData, Key, SimpleValue}
import advxml.core.transform.XmlZoom
import advxml.implicits.root
import org.scalacheck.Gen

import scala.xml._

/** Advxml Created by geirolad on 12/07/2019.
  *
  * @author
  *   geirolad
  */
object XmlGenerator {

  case class BasicXmlElem(name: String, attrs: List[AttributeData], children: Seq[BasicXmlElem]) {
    def toElem: Elem = {
      val seed: MetaData = Null
      val meta: MetaData = this.attrs.foldLeft(seed) { case (acc, data) =>
        new UnprefixedAttribute(
          key   = data.key.value,
          value = data.value.get,
          next  = acc
        )
      }

      Elem(
        prefix        = null,
        label         = this.name,
        attributes    = meta,
        scope         = TopScope,
        minimizeEmpty = false,
        child         = this.children.map(_.toElem): _*
      )
    }
  }

  case class XmlElemGeneratorConfig(
    nameMaxSize: Int            = 10,
    childMaxSize: Int           = 5,
    attrsMaxSize: Int           = 50,
    attrsMaxNameSize: Int       = 5,
    probabilityToHaveAttrs: Int = 80,
    probabilityToHaveChild: Int = 70
  )

  def genAttrsData(maxSize: Int, nameMaxSize: Int): Gen[List[AttributeData]] = {
    for {
      n <- Gen.choose(1, atLeastOne(maxSize))
      kvGen = for {
        key   <- genStr(nameMaxSize).map(Key(_))
        value <- genStr(nameMaxSize).map(SimpleValue(_))
      } yield AttributeData(key, value)
      map <- Gen.listOfN(n, kvGen)
    } yield map
  }

  def genZoom(wholeDocument: Node, probabilityToGoAhead: Int = 80): Gen[XmlZoom] = {

    def rec(current: Node, zoomPath: Gen[XmlZoom]): Gen[XmlZoom] =
      for {
        goAhead <- {
          val falseFrequency = 100 - probabilityToGoAhead
          Gen.frequency[Boolean]((probabilityToGoAhead, true), (falseFrequency, false))
        }
        isCurrentlyEmpty <- zoomPath.map(_.actions.isEmpty)
        zoomStep <- {
          if (current.child.nonEmpty && (goAhead || isCurrentlyEmpty))
            Gen
              .oneOf(current.child)
              .flatMap(selectedNode => zoomPath.map(_.down(selectedNode.label)))
          else
            zoomPath
        }
      } yield zoomStep

    rec(wholeDocument, root)
  }

  def genElem(config: XmlElemGeneratorConfig = XmlElemGeneratorConfig()): Gen[BasicXmlElem] = {
    def rec(level: Int): Gen[BasicXmlElem] = {
      for {
        nodeName <- genStr(config.nameMaxSize)

        attrs <- for {
          hasAttrs <- Gen.frequency(
            (config.probabilityToHaveAttrs, true),
            (100 - config.probabilityToHaveAttrs, false)
          )
          attrs <-
            if (hasAttrs)
              genAttrsData(maxSize = config.attrsMaxSize, nameMaxSize = config.attrsMaxNameSize)
            else
              Gen.const(List.empty[AttributeData])
        } yield attrs

        children <- for {
          hasChildren <- Gen.frequency(
            (config.probabilityToHaveChild, true),
            (100 - config.probabilityToHaveChild, false)
          )
          children <-
            if (level < config.childMaxSize && hasChildren)
              Gen.lzy(Gen.resize(config.childMaxSize, Gen.nonEmptyListOf(rec(level + 1))))
            else
              Gen.const(List.empty)
        } yield children

      } yield BasicXmlElem(nodeName, attrs, children)
    }

    rec(0)
  }

  private def genStr(size: Int): Gen[String] =
    Gen.listOfN(atLeastOne(size), Gen.alphaChar).map(_.mkString)

  private val atLeastOne: Int => Int = v => if (v < 1) 1 else v
}
