package advxml.core.transform

import advxml.core.data.{AttributeData, KeyValuePredicate}
import advxml.core.data.Predicate.alwaysTrue
import advxml.testUtils.generators.XmlGenerator
import advxml.testUtils.generators.XmlGenerator.XmlElemGeneratorConfig
import cats.data.NonEmptyList
import org.scalacheck.{Arbitrary, Properties}
import org.scalacheck.Prop.forAll
import org.scalactic.TypeCheckedTripleEquals.convertToCheckingEqualizer

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

/** Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlTransformationSpec extends Properties("XmlTransformationSpec") {

  import advxml.implicits._
  import advxml.testUtils.ScalacticXmlEquality._
  import cats.instances.try_._

  implicit val elemArbitrary: Arbitrary[Elem] = Arbitrary(
    XmlGenerator
      .genElem(
        XmlElemGeneratorConfig(
          childMaxSize = 1,
          attrsMaxSize = 1,
          attrsMaxNameSize = 3
        )
      )
      .map(_.toElem)
  )

  implicit val attrsDataArbitrary: Arbitrary[NonEmptyList[AttributeData]] = Arbitrary(
    XmlGenerator
      .genAttrsData(10, 5)
      .filter(_.nonEmpty)
      .map(NonEmptyList.fromListUnsafe)
  )

  property("Prepend") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.genZoom(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Prepend(newElem)
    val result: NodeSeq = base.transform[Try](rule).get
    val targetUpdated: NodeSeq = zoom.run[Try](result).get

    (targetUpdated \ newElem.label).exists(_ === newElem)
  }

  property("Append") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.genZoom(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Append(newElem)
    val result: NodeSeq = base.transform[Try](rule).get
    val targetUpdated: NodeSeq = zoom.run[Try](result).get

    (targetUpdated \ newElem.label).exists(_ === newElem)
  }

  property("Replace") = forAll { (base: Elem, newElem: Elem) =>
    val zoom: XmlZoom = XmlGenerator.genZoom(base).sample.get
    val rule: ComposableXmlRule = zoom ==> Replace(_ => newElem)
    val result: Try[NodeSeq] = base.transform[Try](rule)

    (zoom match {
      case x if x == root => root
      case x              => XmlZoom(x.actions.dropRight(1)).immediateDown(newElem.label)
    }).run[Try](result.get).get === newElem
  }

  property("Remove") = forAll(elemArbitrary.arbitrary.filter(_.child.nonEmpty)) { base: Elem =>
    val zoom: XmlZoom = XmlGenerator
      .genZoom(base)
      .filter(_ != root)
      .sample
      .get

    val rule: FinalXmlRule = zoom ==> Remove
    val result: NodeSeq = base.transform[Try](rule).get

    zoom.run[Option](result).isEmpty
  }

  property("SetAttrs") = forAll { (base: Elem, attrsData: NonEmptyList[AttributeData]) =>
    val rule: ComposableXmlRule = root ==> SetAttrs(attrsData)
    val result: NodeSeq = base.transform[Try](rule).get
    val predicates: NonEmptyList[KeyValuePredicate[String]] = attrsData.map(d => d.key === d.value.data)

    result.exists(attrs(predicates))
  }

  property("RemoveAttrs") = forAll { base: Elem =>
    val rule: ComposableXmlRule = root ==> RemoveAttrs(alwaysTrue)
    val result: NodeSeq = base.transform[Try](rule).get

    result.exists(_.attributes.isEmpty)
  }
}
