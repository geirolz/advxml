package advxml.core.transform

import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.testUtils.{ContractTests, FeatureSpecContract}
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlContentZoomTest extends AnyFeatureSpec with FeatureSpecContract {

  import advxml.instances._
  import advxml.instances.convert._

  // format: off
  XmlContentZoomTest.Contract[Try](
    "Mandatory",
    {
      ContractFuncs(
        attribute   = XmlContentZoom.attr[Try, String](_, _),
        text        = XmlContentZoom.text[Try, String](_)
      )
    }
  )(XmlContentZoomTest.TryExtractor).runAll()
  // format: on

  // format: off
  XmlContentZoomTest.Contract[Option](
    "Optional",
    {
      ContractFuncs(
        attribute   = XmlContentZoom.attr[Option, String](_, _),
        text        = XmlContentZoom.text[Option, String](_)
      )
    }
  )(XmlContentZoomTest.OptionExtractor).runAll()
  // format: on
}

object XmlContentZoomTest {

  trait Extractor[F[_]] {
    def extract[T](fa: F[T]): T
    def isFailure[T](fa: F[T]): Boolean
  }

  case object TryExtractor extends Extractor[Try] {
    override def extract[T](fa: Try[T]): T = fa.get
    override def isFailure[T](fa: Try[T]): Boolean = fa.isFailure
  }

  case object OptionExtractor extends Extractor[Option] {
    override def extract[T](fa: Option[T]): T = fa.get
    override def isFailure[T](fa: Option[T]): Boolean = fa.isEmpty
  }

  case class ContractFuncs[F[_]](
    attribute: (NodeSeq, String) => F[String],
    text: NodeSeq => F[String]
  )

  case class Contract[F[_]](subDesc: String, f: ContractFuncs[F])(ex: Extractor[F])
      extends ContractTests("XmlContentZoom", subDesc) {

    test("attribute") {
      val elem: Elem = <foo value="1"></foo>
      assert(ex.extract(f.attribute(elem, "value")) == "1")
      assert(ex.isFailure(f.attribute(elem, "rar")))
    }

    test("text") {
      val elem: Elem = <foo>TEST</foo>
      assert(ex.extract(f.text(elem)) == "TEST")
      assert(ex.isFailure(f.text(<rar/>)))
    }
  }
}
