package advxml.testUtils.generators

import advxml.core.transform.actions.XmlZoom
import org.scalacheck.Gen

import scala.xml._

/** Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlGenerator {

  case class XmlGeneratorConfig(
    nameMaxSize: Int = 10,
    childMaxSize: Int = 5,
    attrsMaxSize: Int = 50,
    attrsMaxNameSize: Int = 5,
    probabilityToHaveAttrs: Int = 80,
    probabilityToHaveChild: Int = 70
  )

  case class BasicXmlElem(name: String, attrs: Map[String, String], children: Seq[BasicXmlElem]) {
    def toElem: Elem = {
      val seed: MetaData = Null
      val meta: MetaData = this.attrs.toList.foldLeft(seed) { case (acc, (s1, s2)) =>
        new UnprefixedAttribute(
          key = s1,
          value = s2,
          next = acc
        )
      }

      Elem(
        prefix = null,
        label = this.name,
        attributes = meta,
        scope = TopScope,
        minimizeEmpty = false,
        child = this.children.map(_.toElem): _*
      )
    }
  }

  def xmlZoomGenerator(wholeDocument: Node): Gen[XmlZoom] = {

    def rec(current: Node, zoomPath: Gen[XmlZoom]): Gen[XmlZoom] =
      for {
        goAhead          <- Gen.frequency[Boolean]((80, true), (20, false))
        isCurrentlyEmpty <- zoomPath.map(_.zoomActions.isEmpty)
        zoomStep <- {
          if (goAhead || isCurrentlyEmpty)
            Gen
              .oneOf(current.child)
              .flatMap(selectedNode => zoomPath.map(_.immediateDown(selectedNode.label)))
          else
            zoomPath
        }
      } yield zoomStep

    rec(wholeDocument, XmlZoom.root)
  }

  def xmlElemGenerator()(implicit config: XmlGeneratorConfig = XmlGeneratorConfig()): Gen[BasicXmlElem] = {
    def rec(level: Int): Gen[BasicXmlElem] = {
      for {
        nodeName <- genStr(config.nameMaxSize)

        attrs <- for {
          hasAttrs <- Gen.frequency((config.probabilityToHaveAttrs, true), (100 - config.probabilityToHaveAttrs, false))
          attrs <-
            if (hasAttrs)
              attrsGenerator(maxSize = config.attrsMaxSize, nameMaxSize = config.attrsMaxNameSize)
            else
              Gen.const(Map.empty[String, String])
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

  private def genStr(size: Int): Gen[String] = Gen.listOfN(size, Gen.alphaChar).map(_.mkString)

  private def attrsGenerator(maxSize: Int, nameMaxSize: Int): Gen[Map[String, String]] = {
    for {
      n <- Gen.choose(1, maxSize)
      kvGen = for {
        key   <- genStr(nameMaxSize)
        value <- genStr(nameMaxSize)
      } yield (key, value)
      map <- Gen.mapOfN(n, kvGen)
    } yield map
  }
}
