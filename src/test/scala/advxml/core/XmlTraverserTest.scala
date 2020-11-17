package advxml.core

import advxml.core.XmlTraverserTest.ContractFuncs
import advxml.core.data.XmlPredicate
import advxml.testUtils.{ContractTests, FeatureSpecContract}
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.Assertions.convertToEqualizer

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlTraverserTest extends AnyFeatureSpec with FeatureSpecContract {

  // format: off
  XmlTraverserTest.Contract[Try](
    "Mandatory",
    {
      import advxml.instances.traverse._
      import cats.instances.try_._

      ContractFuncs(
        immediateChild  = (doc, nodeName) => XmlTraverser[Try].immediateChildren(doc, nodeName),
        children        = (doc, nodeName) => XmlTraverser[Try].children(doc, nodeName),
        attribute       = (doc, attrName) => XmlTraverser[Try].attr(doc, attrName),
        text            = XmlTraverser[Try].text(_),
        trimmedText     = XmlTraverser[Try].trimmedText(_),
        atIndex         = (doc, idx) => XmlTraverser[Try].childTraverser.atIndex(doc, idx),
        head            = XmlTraverser[Try].childTraverser.head(_),
        last            = XmlTraverser[Try].childTraverser.last(_),
        tail            = XmlTraverser[Try].childTraverser.tail(_),
        find            = (doc, p) => XmlTraverser[Try].childTraverser.find(doc, p),
        filter          = (doc, p) => XmlTraverser[Try].childTraverser.filter(doc, p)
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Optional",
    {
      import advxml.instances.traverse._
      import cats.instances.option._

      ContractFuncs(
        immediateChild      = (doc, nodeName) => XmlTraverser[Option].immediateChildren(doc, nodeName),
        children            = (doc, nodeName) => XmlTraverser[Option].children(doc, nodeName),
        attribute           = (doc, attrName) => XmlTraverser[Option].attr(doc, attrName),
        text                = XmlTraverser[Option].text(_),
        trimmedText         = XmlTraverser[Option].trimmedText(_),
        atIndex             = (doc, idx) => XmlTraverser[Option].childTraverser.atIndex(doc, idx),
        head                = XmlTraverser[Option].childTraverser.head(_),
        last                = XmlTraverser[Option].childTraverser.last(_),
        tail                = XmlTraverser[Option].childTraverser.tail(_),
        find                = (doc, p) => XmlTraverser[Option].childTraverser.find(doc, p),
        filter              = (doc, p) => XmlTraverser[Option].childTraverser.filter(doc, p)
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
    trimmedText: NodeSeq => F[String],
    atIndex: (Elem, Int) => F[NodeSeq],
    head: Elem => F[NodeSeq],
    last: Elem => F[NodeSeq],
    tail: Elem => F[NodeSeq],
    find: (Elem, XmlPredicate) => F[NodeSeq],
    filter: (Elem, XmlPredicate) => F[NodeSeq]
  )

  case class Contract[F[_]](subDesc: String, f: ContractFuncs[F])(ex: Extractor[F])
      extends ContractTests("XmlTraverser", subDesc) {

    import advxml.testUtils.ScalacticXmlEquality._

    test("immediateChild") {
      val elem: Elem = <foo><bar value="1"/></foo>
      assert(ex.extract(f.immediateChild(elem, "bar")) === <bar value="1"/>)
      assert(ex.isFailure(f.immediateChild(elem, "rar")))
    }

    test("children") {
      val elem: Elem = <foo><test><bar value="1"/></test></foo>
      assert(ex.extract(f.children(elem, "bar")) === <bar value="1"/>)
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

    test("atIndexF") {
      val elem: Elem =
        <foo>
          <bar value="1"/>
          <bar value="2"/>
        </foo>

      assert(ex.extract(f.atIndex(elem, 1)) === <bar value="2"/>)
      assert(ex.isFailure(f.atIndex(elem, 3)))
    }

    test("headF") {
      val elem: Elem =
        <foo>
          <bar value="1"/>
          <bar value="2"/>
        </foo>

      assert(ex.extract(f.head(elem)) === <bar value="1"/>)
    }

    test("lastF") {
      val elem: Elem =
        <foo>
          <bar value="1"/>
          <bar value="2"/>
        </foo>

      assert(ex.extract(f.last(elem)) === <bar value="2"/>)
    }

    test("tailF") {
      val elem: Elem =
        <foo>
          <bar value="1"/>
          <bar value="2"/>
          <bar value="3"/>
        </foo>

      assert(ex.extract(f.tail(elem)) === NodeSeq.fromSeq(<bar value="2"/><bar value="3"/>))
    }

    test("findF") {
      val elem: Elem =
        <foo>
          <bar value="1"/>
          <bar value="2"/>
          <bar value="3"/>
        </foo>

      assert(ex.extract(f.find(elem, _ \@ "value" == "2")) === <bar value="2"/>)
    }

    test("filterF") {
      val elem: Elem =
        <foo>
          <bar value="2"/>
          <bar value="3"/>
          <bar value="4"/>
        </foo>

      assert(
        ex.extract(f.filter(elem, e => (e \@ "value").toInt % 2 == 0)) === NodeSeq.fromSeq(
          <bar value="2"/><bar value="4"/>
        )
      )
    }
  }
}
