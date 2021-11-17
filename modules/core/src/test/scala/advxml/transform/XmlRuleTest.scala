package advxml.transform

import advxml.transform.XmlZoom.root
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.NodeSeq

class XmlRuleTest extends AnyFunSuite {

  import advxml.transform.XmlModifier.*
  import advxml.implicits.*

  test("XmlRule.apply with varargs") {
    val rule: ComposableXmlRule = XmlRule(root, SetAttrs(k"T1" := 1), SetAttrs(k"T2" := 2))
    assert(rule.modifiers.size == 2)
  }

  test("XmlRule.apply with List") {
    val rule: ComposableXmlRule = XmlRule(
      root,
      List(
        SetAttrs(k"T1" := 1),
        SetAttrs(k"T2" := 2)
      )
    )

    assert(rule.modifiers.size == 2)
  }

  test("XmlRule.apply with FinalXmlModifier") {
    val rule: FinalXmlRule = XmlRule(
      root,
      Remove
    )

    assert(rule.modifier == Remove)
  }

  test("ComposableXmlRule.withModifier") {
    val rule: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T1" := 1)
    )

    val updatedRule: ComposableXmlRule = rule.withModifier(SetAttrs(k"T2" := 2))

    assert(updatedRule.modifiers.size == 2)
  }

  test("XmlRule.transform with varargs") {
    val rule1: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T1" := 1)
    )
    val rule2: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T2" := 2)
    )

    val result: NodeSeq = AbstractRule.transform[Try](<Root></Root>, rule1, rule2).get
    assert(result === <Root T2="2" T1="1"></Root>)
  }

  test("XmlRule.transform with List") {
    val rule1: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T1" := 1)
    )
    val rule2: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T2" := 2)
    )

    val result: NodeSeq = AbstractRule.transform[Try](<Root></Root>, List(rule1, rule2)).get
    assert(result === <Root T2="2" T1="1"></Root>)
  }

  test("XmlRule.transform with single XmlRule") {
    val rule: ComposableXmlRule = XmlRule(
      root,
      SetAttrs(k"T1" := 1)
    )

    val result: NodeSeq = XmlRule.transform[Try](<Root></Root>, rule).get
    assert(result === <Root T1="1"></Root>)
  }
}
