package advxml.core.transform

import advxml.core.transform.actions.XmlZoom
import advxml.testUtils.generators.XmlGenerator
import advxml.testUtils.generators.XmlGenerator.XmlGeneratorConfig
import org.scalacheck.{Arbitrary, Properties}
import org.scalacheck.Prop.forAll

import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq}

/** Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlTransformationSpec extends Properties("XmlTransformationSpec") {

  //noinspection RedundantDefaultArgument
  implicit val xmlGenConfig: XmlGeneratorConfig = XmlGeneratorConfig(
    childMaxSize = 1,
    attrsMaxSize = 1,
    attrsMaxNameSize = 3
  )

  implicit val elemGenerator: Arbitrary[Elem] = Arbitrary(
    XmlGenerator
      .xmlElemGenerator()
      .filter(_.children.nonEmpty)
      .map(_.toElem)
  )

  import advxml.implicits._
  import cats.instances.try_._

  property("Prepend") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.xmlZoomGenerator(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Prepend(newElem)
    val result: Try[NodeSeq] = base.transform[Try](rule)
    val targetUpdated: NodeSeq = result
      .flatMap(zoom(_))
      .map(_.nodeSeq)
      .get

    (targetUpdated \ newElem.label).nonEmpty
  }

  property("Append") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.xmlZoomGenerator(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Append(newElem)
    val result: Try[NodeSeq] = base.transform[Try](rule)
    val targetUpdated: NodeSeq = result
      .flatMap(zoom(_))
      .map(_.nodeSeq)
      .get

    (targetUpdated \ newElem.label).nonEmpty
  }

  property("Replace") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.xmlZoomGenerator(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Replace(_ => newElem)
    val result: Try[NodeSeq] = base.transform[Try](rule)

    result.flatMap(zoom(_)).isFailure
  }

  property("Remove") = forAll { base: Elem =>
    val zoom: XmlZoom = XmlGenerator.xmlZoomGenerator(base).sample.get
    val rule: FinalXmlRule = zoom ==> Remove
    val result: Try[Node] = Try(base.transform[Try](rule).get.head)

    result.flatMap(zoom(_)).isFailure
  }
}
