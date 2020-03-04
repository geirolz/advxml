package advxml.core

import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlTraverseTest extends AnyFeatureSpec with XmlTraverserContractAsserts {

  Feature("XmlTraverse.Mandatory") {

    import cats.instances.try_._

    Scenario("\\!") {
      mandatory.assertImmediateChild((nodeName, doc) => XmlTraverser.mandatory[Try].immediateChildren(doc, nodeName))
    }

    Scenario("\\\\!") {
      mandatory.assertChildren((nodeName, doc) => XmlTraverser.mandatory[Try].children(doc, nodeName))
    }
    Scenario("\\@!") {
      mandatory.assertAttribute((attrName, doc) => XmlTraverser.mandatory[Try].attr(doc, attrName))
    }
    Scenario("!") {
      mandatory.assertText(XmlTraverser.mandatory[Try].text(_))
    }
    Scenario("|!|") {
      mandatory.assertText(XmlTraverser.mandatory[Try].trimmedText(_))
    }
  }

  Feature("XmlTraverse.Optional") {

    import cats.instances.option._

    Scenario("\\?") {
      optional.assertImmediateChild((nodeName, doc) => XmlTraverser.optional[Option].immediateChildren(doc, nodeName))
    }

    Scenario("\\\\?") {
      optional.assertChildren((nodeName, doc) => XmlTraverser.optional[Option].children(doc, nodeName))
    }
    Scenario("\\@?") {
      optional.assertAttribute((attrName, doc) => XmlTraverser.optional[Option].attr(doc, attrName))
    }
    Scenario("?") {
      optional.assertText(XmlTraverser.optional[Option].text(_))
    }
    Scenario("|?|") {
      optional.assertText(XmlTraverser.optional[Option].trimmedText(_))
    }
  }
}

//Contract test
private[advxml] trait XmlTraverserContractAsserts {

  import advxml.syntax.normalize._

  sealed trait TypedXmlTraverseAsserts[F[_]] {

    def extract[T](fa: F[T]): T

    def isFailure[T](fa: F[T]): Boolean

    def assertImmediateChild(f: (String, NodeSeq) => F[NodeSeq]): Unit = {
      val elem: Elem = <foo><bar value="1"/></foo>
      assert(extract(f("bar", elem)) |==| <bar value="1"/>)
      assert(isFailure(f("rar", elem)))
    }

    def assertChildren(f: (String, NodeSeq) => F[NodeSeq]): Unit = {
      val elem: Elem = <foo><test><bar value="1"/></test></foo>
      assert(extract(f("bar", elem)) |==| <bar value="1"/>)
      assert(isFailure(f("rar", elem)))
    }

    def assertAttribute(f: (String, NodeSeq) => F[String]): Unit = {
      val elem: Elem = <foo value="1"></foo>
      assert(extract(f("value", elem)) == "1")
      assert(isFailure(f("rar", elem)))
    }

    def assertText(f: NodeSeq => F[String]): Unit = {
      val elem: Elem = <foo>TEST</foo>
      assert(extract(f(elem)) == "TEST")
      assert(isFailure(f(<rar/>)))
    }

    def assertTrimmedText(f: NodeSeq => F[String]): Unit = {
      val elem: Elem = <foo> TEST </foo>
      assert(extract(f(elem)) == "TEST")
      assert(isFailure(f(<rar/>)))
    }
  }

  object mandatory extends TypedXmlTraverseAsserts[Try] {
    override def extract[T](fa: Try[T]): T = fa.get
    override def isFailure[T](fa: Try[T]): Boolean = fa.isFailure
  }
  object optional extends TypedXmlTraverseAsserts[Option] {
    override def extract[T](fa: Option[T]): T = fa.get
    override def isFailure[T](fa: Option[T]): Boolean = fa.isEmpty
  }
}
