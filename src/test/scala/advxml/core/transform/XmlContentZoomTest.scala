package advxml.core.transform

import advxml.core.data.{ValidatedValue, Value}
import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.core.transform.XmlZoom.root
import advxml.core.AppExOrEu
import advxml.testUtils.{ContractTests, Fallible, FeatureSpecContract}
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlContentZoomTest extends AnyFeatureSpec with FeatureSpecContract {

  import cats.instances.try_._

  // format: off
  XmlContentZoomTest.Contract[Try](
    "Core",
    {
      ContractFuncs[Try](
        //label
        label                  = ns => XmlContentZoom.label(ns),
        labelFromBindedZoom    = zoom => XmlContentZoom.labelFromBindedZoom(zoom).extract[Try],
        labelFromZoom          = (zoom, ns) => XmlContentZoom.labelFromZoom(zoom, ns).extract[Try],
        //attr
        attr                  = (ns, key) => XmlContentZoom.attr(ns, key),
        attrFromBindedZoom    = (zoom, key) => XmlContentZoom.attrFromBindedZoom(zoom, key).extract[Try],
        attrFromZoom          = (zoom, ns, key) => XmlContentZoom.attrFromZoom(zoom, ns, key).extract[Try],
        //content
        content               = ns => XmlContentZoom.content(ns),
        contentFromBindedZoom = zoom => XmlContentZoom.contentFromBindedZoom(zoom).extract[Try],
        contentFromZoom       = (zoom, ns) => XmlContentZoom.contentFromZoom(zoom, ns).extract[Try]
      )
    }
  ).runAll()
  // format: on
}

object XmlContentZoomTest {

  case class ContractFuncs[F[_]](
    //attr
    label: NodeSeq => Value,
    labelFromBindedZoom: BindedXmlZoom => F[String],
    labelFromZoom: (XmlZoom, NodeSeq) => F[String],
    //attr
    attr: (NodeSeq, String) => ValidatedValue,
    attrFromBindedZoom: (BindedXmlZoom, String) => F[String],
    attrFromZoom: (XmlZoom, NodeSeq, String) => F[String],
    //text
    content: NodeSeq => ValidatedValue,
    contentFromBindedZoom: BindedXmlZoom => F[String],
    contentFromZoom: (XmlZoom, NodeSeq) => F[String]
  )

  case class Contract[F[_]: AppExOrEu: Fallible](subDesc: String, f: ContractFuncs[F])
      extends ContractTests("XmlContentZoom", subDesc) {

    import Fallible._

    //============================= LABEL =============================
    test("label from NodeSeq") {
      val elem: Elem = <foo value="1"></foo>
      assert(f.label(elem).unboxed == "foo")
    }

    test("label from BindedXmlZoom") {
      val elem: BindedXmlZoom = root(<foo value="1"></foo>)
      assert(f.labelFromBindedZoom(elem).extract == "foo")
    }

    test("label from (Unbinded)XmlZoom") {
      val elem: NodeSeq = <foo value="1"></foo>
      assert(f.labelFromZoom(root, elem).extract == "foo")
    }

    //============================= ATTR =============================
    test("attribute from NodeSeq") {
      val elem: Elem = <foo value="1"></foo>
      assert(f.attr(elem, "value").extract[F].extract == "1")
    }

    test("attribute from BindedXmlZoom") {
      val elem: BindedXmlZoom = root(<foo value="1"></foo>)

      assert(f.attrFromBindedZoom(elem, "value").extract == "1")
      assert(f.attrFromBindedZoom(elem, "rar").isFailure)
    }

    test("attribute from (Unbinded)XmlZoom") {
      val elem: NodeSeq = <foo value="1"></foo>

      assert(f.attrFromZoom(root, elem, "value").extract == "1")
      assert(f.attrFromZoom(root, elem, "rar").isFailure)
    }

    //============================ CONTENT ============================
    test("content from NodeSeq") {
      val elemWithContent: Elem = <foo>TEST</foo>
      val elemWithoutContent = <foo></foo>

      assert(f.content(elemWithContent).extract[F].extract == "TEST")
      assert(f.content(elemWithoutContent).extract[F].isFailure)
    }

    test("content from BindedXmlZoom") {
      val elemWithContent: BindedXmlZoom = root(<foo>TEST</foo>)
      val elemWithoutContent: BindedXmlZoom = root(<foo></foo>)

      assert(f.contentFromBindedZoom(elemWithContent).extract == "TEST")
      assert(f.contentFromBindedZoom(elemWithoutContent).isFailure)
    }

    test("content from (Unbinded)XmlZoom") {
      val elemWithContent: Elem = <foo>TEST</foo>
      val elemWithoutContent = <foo></foo>

      assert(f.contentFromZoom(root, elemWithContent).extract == "TEST")
      assert(f.contentFromZoom(root, elemWithoutContent).isFailure)
    }
  }
}
