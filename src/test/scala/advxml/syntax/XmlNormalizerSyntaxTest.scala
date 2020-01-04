package advxml.syntax

import advxml.core.XmlNormalizerAsserts
import org.scalatest.{Assertion, FunSuite}

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 19/07/2019.
  *
  * @author geirolad
  */
class XmlNormalizerSyntaxTest extends FunSuite with XmlNormalizerAsserts {

  import advxml.syntax.normalize._

  test("XmlNormalizer - Normalize | with Elem") {
    assert_normalized_Equals(n => n.normalize)
  }

  test("XmlNormalizer - Normalize | with Comment | NOT SUPPORTED -> NO ACTIONS") {
    assert_normalized_unsupported_NodeSeq(_.normalize)
  }

  test("XmlNormalizer - Equality") {
    assert_equality_Equals((v1, v2) => v1 |==| v2)
  }

  test("XmlNormalizer - Equality | Not equals") {
    assert_equality_notEquals((v1, v2) => v1 |!=| v2)
  }

  test("XmlNormalizer - Equality | with Scalatric") {
    assert_equality_Equals((v1, v2) => v1 === v2)
  }

  test("XmlNormalizer - Equality | Not equals | with Scalatric") {
    assert_equality_notEquals((v1, v2) => v1 !== v2)
  }

  private def assert_equality_notEquals(p: (NodeSeq, NodeSeq) => Boolean): Assertion = {
    val v1 =
      <Cars>
        <Car V1="1"/>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="3" ></Car>
      </Cars>

    assert(p(v1, v2))
  }
}
