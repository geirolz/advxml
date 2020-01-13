package advxml.core

import org.scalatest.FunSuite

import scala.xml.{Comment, Group, NodeSeq}

class XmlNormalizerTest extends FunSuite with XmlNormalizerAsserts {

  test("XmlNormalizer - Normalize | with Elem") {
    assert_normalized_Equals(n => XmlNormalizer.normalize(n))
  }

  test("XmlNormalizer - Normalize | with Comment | NOT SUPPORTED -> NO ACTIONS") {
    assert_normalized_unsupported_NodeSeq(XmlNormalizer.normalize)
  }

  test("XmlNormalizer - Equality") {
    assert_equality_Equals((v1, v2) => XmlNormalizer.normalizedEquals(v1, v2))
  }
}

private[advxml] trait XmlNormalizerAsserts {

  def assert_normalized_Equals(f: NodeSeq => NodeSeq): Unit = {
    val v1 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"></Property>
            <Property K="3">TEXT</Property>
          </Properties>
        </Car>
        <Car V1="4" ></Car>
        <Car V1="5" >TEXT</Car>
      </Cars>

    val expected =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"/>
            <Property K="3">TEXT</Property>
          </Properties>
        </Car>
        <Car V1="4" />
        <Car V1="5" >TEXT</Car>
      </Cars>

    assert(f(v1) xml_sameElements XmlNormalizer.normalize(expected))
  }

  def assert_normalized_unsupported_NodeSeq(f: NodeSeq => NodeSeq): Unit = {
    val data = Group(Comment("TEST"))
    assert(f(data) xml_sameElements data)
  }

  def assert_equality_Equals(p: (NodeSeq, NodeSeq) => Boolean): Unit = {
    val v1 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"/>
          </Properties>
        </Car>
        <Car V1="3"/>
      </Cars>

    val v2 =
      <Cars>
        <Car V1="1" />
        <Car V1="2" >
          <Properties>
            <Property K="1"/>
            <Property K="2"></Property>
          </Properties>
        </Car>
        <Car V1="3" ></Car>
      </Cars>

    assert(p(v1, v2))
  }
}
