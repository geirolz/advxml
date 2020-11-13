package advxml.instances

import advxml.core.transform.actions.XmlPredicate.XmlPredicate
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.{Document, Elem, Group, NodeSeq}

class XmlPredicateInstancesTest extends AnyFunSuite {

  import advxml.instances.transform.predicates._
  import advxml.syntax._

  test("Test 'always' predicate") {
    assert(alwaysTrue(<Node>TEST</Node>))
  }

  test("Test 'text' predicate") {
    val p: XmlPredicate = text(_ == "TEST")
    val target: Elem = <Node>TEST</Node>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'label' predicate - w/ Node") {
    val p: XmlPredicate = label(_ == "Node")
    val target: Elem = <Node/>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'label' predicate - with Document") {
    val p: XmlPredicate = label(_ == "Node")
    val target: Document = new Document()
    val result: Boolean = p(target)
    assert(!result)
  }

  test("Test 'attrs' predicate") {
    val p: XmlPredicate = attrs(k"A1" === "TEST")
    val target: Elem = <Node A1="TEST"/>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'hasImmediateChild' predicate") {
    val p: XmlPredicate = hasImmediateChild("SubNode")
    val target: Elem = <Node><SubNode/></Node>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'hasImmediateChild' predicate - with Predicate") {
    val p: XmlPredicate = hasImmediateChild("SubNode", text(_ == "TEST"))
    val target: Elem = <Node><SubNode>TEST</SubNode></Node>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'strictEqualsTo' predicate - with Node-Node") {
    val p: XmlPredicate = strictEqualsTo(<Node><SubNode>TEST</SubNode></Node>)
    val target: Elem = <Node><SubNode>TEST</SubNode></Node>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'strictEqualsTo' predicate - with Group-Group") {
    val p: XmlPredicate = strictEqualsTo(Group(<Node><SubNode>TEST</SubNode></Node>))
    val target: Group = Group(<Node><SubNode>TEST</SubNode></Node>)
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'strictEqualsTo' predicate - with Group-Node") {
    val p: XmlPredicate = strictEqualsTo(<Node><SubNode>TEST</SubNode></Node>)
    val target: Group = Group(<Node><SubNode>TEST</SubNode></Node>)
    val result: Boolean = p(target)
    assert(!result)
  }

  test("Test 'strictEqualsTo' predicate - with NodeSeq-NodeSeq") {
    val ns1: NodeSeq = <Node><SubNode>TEST</SubNode></Node>.asInstanceOf[NodeSeq]
    val ns2: NodeSeq = <Node><SubNode>TEST</SubNode></Node>.asInstanceOf[NodeSeq]
    assert(strictEqualsTo(ns1)(ns2))
  }
}
