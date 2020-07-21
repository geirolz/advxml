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
      import cats.instances.try_._
      import advxml.instances.traverse._
      
      ContractFuncs(
        immediateChild  = (doc, nodeName) => XmlTraverser[Try].immediateChildren(doc, nodeName),
        children        = (doc, nodeName) => XmlTraverser[Try].children(doc, nodeName),
        attribute       = (doc, attrName) => XmlTraverser[Try].attr(doc, attrName),
        text            = XmlTraverser[Try].text(_),
        trimmedText     = XmlTraverser[Try].trimmedText(_),
        atIndex         = (doc, idx) => XmlTraverser[Try].atIndexF(doc, idx),
        head            = XmlTraverser[Try].headF(_),
        last            = XmlTraverser[Try].lastF(_)
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Optional",
    {
      import cats.instances.option._
      import advxml.instances.traverse._
      
      ContractFuncs(
        immediateChild      = (doc, nodeName) => XmlTraverser[Option].immediateChildren(doc, nodeName),
        children            = (doc, nodeName) => XmlTraverser[Option].children(doc, nodeName),
        attribute           = (doc, attrName) => XmlTraverser[Option].attr(doc, attrName),
        text                = XmlTraverser[Option].text(_),
        trimmedText         = XmlTraverser[Option].trimmedText(_),
        atIndex             = (doc, idx) => XmlTraverser[Option].atIndexF(doc, idx),
        head                = XmlTraverser[Option].headF(_),
        last                = XmlTraverser[Option].lastF(_)
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()
  // format: on
}

//class XmlDynamicTraverserTest extends AnyFeatureSpec with FeatureSpecContract {
//
//  // format: off
//  XmlTraverserTest.Contract[Try](
//    "Dynamic_Mandatory",
//    {
//      import cats.instances.try_._
//      ContractFuncs(
//        immediateChild  = (doc, nodeName) => XmlDynamicTraverser.mandatory.immediate(doc).selectDynamic(nodeName).get,
//        children        = (doc, nodeName) => XmlDynamicTraverser.mandatory.deep(doc).selectDynamic(nodeName).get,
//        attribute       = (doc, attrName) => XmlDynamicTraverser.mandatory.immediate(doc).attr(attrName),
//        text            = XmlDynamicTraverser.mandatory.immediate(_).text,
//        trimmedText     = XmlDynamicTraverser.mandatory.immediate(_).trimmedText,
//        atIndex         = (doc, idx) => XmlDynamicTraverser.mandatory.immediate(doc).atIndex(idx).get,
//        head            = XmlDynamicTraverser.mandatory.immediate(_).head.get,
//        last            = XmlDynamicTraverser.mandatory.immediate(_).last.get
//      )
//    }
//  )(XmlTraverserTest.TryExtractor).runAll()
//
//  XmlTraverserTest.Contract[Option](
//    "Dynamic_Optional",
//    {
//      import cats.instances.option._
//      ContractFuncs(
//        immediateChild      = (doc, nodeName) => XmlDynamicTraverser.optional.immediate(doc).selectDynamic(nodeName).get,
//        children            = (doc, nodeName) => XmlDynamicTraverser.optional.deep(doc).selectDynamic(nodeName).get,
//        attribute           = (doc, attrName) => XmlDynamicTraverser.optional.immediate(doc).attr(attrName),
//        text                = doc => XmlDynamicTraverser.optional.immediate(doc).text,
//        trimmedText         = doc => XmlDynamicTraverser.optional.immediate(doc).trimmedText,
//        atIndex             = (doc, idx) => XmlDynamicTraverser.optional.immediate(doc).atIndex(idx).get,
//        head                = XmlDynamicTraverser.optional.immediate(_).head.get,
//        last                = XmlDynamicTraverser.optional.immediate(_).last.get
//      )
//    }
//  )(XmlTraverserTest.OptionExtractor).runAll()
//  // format: on
//}
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
    trimmedText: NodeSeq => F[String],
    atIndex: (NodeSeq, Int) => F[NodeSeq],
    head: NodeSeq => F[NodeSeq],
    last: NodeSeq => F[NodeSeq]
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
