package advxml.instances

import advxml.core.data.XmlPredicate
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.{Document, Elem, Group, NodeSeq}

class TransformXmlPredicateInstancesTest extends AnyFunSuite {

  import advxml.instances.convert._
  import advxml.instances.transform.predicates._
  import advxml.syntax._
  import advxml.testUtils.ScalacticXmlEquality._

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

  test("Test 'attrs' predicate - with varargs") {
    val p: XmlPredicate = attrs(k"A1" === "TEST")
    val target: Elem = <Node A1="TEST"/>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'attrs' predicate - with NonEmptyList") {
    val p: XmlPredicate = attrs(NonEmptyList.one(k"A1" === "TEST"))
    val target: Elem = <Node A1="TEST"/>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'hasAttrs' predicate - with varargs") {
    val p: XmlPredicate = hasAttrs(k"A1")
    val target: Elem = <Node A1="TEST"/>
    val result: Boolean = p(target)
    assert(result)
  }

  test("Test 'hasAttrs' predicate - with NonEmptyList") {
    val p: XmlPredicate = hasAttrs(NonEmptyList.one(k"A1"))
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

  test("Filter by text") {

    val data: Elem =
      <Persons>
        <Person>David</Person>
        <Person>Marco</Person>
        <Person>Simone</Person>
      </Persons>

    val result: NodeSeq = data \ "Person" filter text(_ == "Simone")

    assert(result === <Person>Simone</Person>)
  }

  test("Filter by label") {

    val data: Elem =
      <Persons>
        <Person1>David</Person1>
        <Person2>Marco</Person2>
        <Person3>Simone</Person3>
      </Persons>

    val result: NodeSeq = data.child filter label(_ == "Person3")

    assert(result === <Person3>Simone</Person3>)
  }

  test("Filter by attrs") {

    val data: Elem =
      <Persons>
        <Person A="1" B="A">David</Person>
        <Person A="2" B="B">Marco</Person>
        <Person A="3" B="C">Simone</Person>
      </Persons>

    val result: NodeSeq = data \ "Person" filter attrs(
      k"A" === "3",
      k"B" === "C"
    )

    assert(result === <Person A="3" B="C">Simone</Person>)
  }

  test("Filter by strict equality") {

    val data: Elem =
      <Persons>
        <Person A="1"></Person>
        <Person A="2"></Person>
        <Person A="3"></Person>
      </Persons>

    val result: NodeSeq = data \ "Person" filter strictEqualsTo(<Person A="3"></Person>)

    assert(result === <Person A="3"></Person>)
  }

  test("Filter by strict equality - minimized empty") {

    val data: Elem =
      <Persons>
        <Person A="1"></Person>
        <Person A="2"></Person>
        <Person A="3"></Person>
      </Persons>

    val result: NodeSeq = data \ "Person" filter strictEqualsTo(<Person A="3"/>)

    assert(result === <Person A="3"></Person>)
  }
}
