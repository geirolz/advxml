package advxml.core

import advxml.core.XmlTraverserTest.ContractFuncs
import advxml.test.{ContractTests, FeatureSpecContract}
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlTraverserTest extends AnyFeatureSpec with FeatureSpecContract {

  // format: off
  XmlTraverserTest.Contract[Try](
    "Mandatory",
    {
      ContractFuncs(
        immediateChild = (doc, nodeName) => XmlTraverser.mandatory.immediateChildren(doc, nodeName),
        children = (doc, nodeName) => XmlTraverser.mandatory.children(doc, nodeName),
        attribute = (doc, attrName) => XmlTraverser.mandatory.attr(doc, attrName),
        text = XmlTraverser.mandatory.text(_),
        trimmedText = XmlTraverser.mandatory.trimmedText(_)
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()
  // format: on

  // format: off
  XmlTraverserTest.Contract[Option](
    "Optional",
    {
      ContractFuncs(
        immediateChild      = (doc, nodeName) => XmlTraverser.optional.immediateChildren(doc, nodeName),
        children            = (doc, nodeName) => XmlTraverser.optional.children(doc, nodeName),
        attribute           = (doc, attrName) => XmlTraverser.optional.attr(doc, attrName),
        text                = XmlTraverser.optional.text(_),
        trimmedText         = XmlTraverser.optional.trimmedText(_)
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()
  // format: on
}

object XmlTraverserTest {

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
    immediateChild: (NodeSeq, String) => F[NodeSeq],
    children: (NodeSeq, String) => F[NodeSeq],
    attribute: (NodeSeq, String) => F[String],
    text: NodeSeq => F[String],
    trimmedText: NodeSeq => F[String]
  )

  case class Contract[F[_]](subDesc: String, f: ContractFuncs[F])(ex: Extractor[F])
      extends ContractTests("XmlTraverser", subDesc) {

    import advxml.syntax.normalize._

    test("immediateChild") {
      val elem: Elem = <foo><bar value="1"/></foo>
      assert(ex.extract(f.immediateChild(elem, "bar")) |==| <bar value="1"/>)
      assert(ex.isFailure(f.immediateChild(elem, "rar")))
    }

    test("children") {
      val elem: Elem = <foo><test><bar value="1"/></test></foo>
      assert(ex.extract(f.children(elem, "bar")) |==| <bar value="1"/>)
      assert(ex.isFailure(f.children(elem, "rar")))
    }

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

    test("trimmedText") {
      val elem: Elem = <foo> TEST </foo>
      assert(ex.extract(f.trimmedText(elem)) == "TEST")
      assert(ex.isFailure(f.trimmedText(<rar/>)))
    }
  }
}
