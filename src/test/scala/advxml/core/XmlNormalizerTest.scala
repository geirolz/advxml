package advxml.core

import advxml.core.XmlNormalizerTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.{Comment, Group, NodeSeq}

class XmlNormalizerTest extends AnyFunSuite with FunSuiteContract {
  XmlNormalizerTest
    .Contract(
      f = ContractFuncs(
        normalizeAction = XmlNormalizer.normalize,
        normalizedEqualsAction = XmlNormalizer.normalizedEquals
      )
    )
    .runAll()
}

object XmlNormalizerTest {

  case class ContractFuncs(normalizeAction: NodeSeq => NodeSeq, normalizedEqualsAction: (NodeSeq, NodeSeq) => Boolean)

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("XmlNormalizer", subDesc) {

    test("Normalize") {
      val v1 =
        <Cars>
          <Car V1="1"/>
          <Car V1="2">
            <Properties>
              <Property K="1"/>
              <Property K="2"></Property>
              <Property K="3">TEXT</Property>
            </Properties>
          </Car>
          <Car V1="4"></Car>
          <Car V1="5">TEXT</Car>
        </Cars>

      val expected =
        <Cars>
          <Car V1="1"/>
          <Car V1="2">
            <Properties>
              <Property K="1"/>
              <Property K="2"/>
              <Property K="3">TEXT</Property>
            </Properties>
          </Car>
          <Car V1="4"/>
          <Car V1="5">TEXT</Car>
        </Cars>

      assert(f.normalizeAction(v1) xml_sameElements XmlNormalizer.normalize(expected))
    }

    test("UnsupportedNormalize") {
      val data = Group(Comment("TEST"))
      assert(f.normalizeAction(data) xml_sameElements data)
    }

    test("Equality.Equals") {
      val v1 =
        <Cars>
          <Car V1="1"/>
          <Car V1="2">
            <Properties>
              <Property K="1"/>
              <Property K="2"/>
            </Properties>
          </Car>
          <Car V1="3"/>
        </Cars>

      val v2 =
        <Cars>
          <Car V1="1"/>
          <Car V1="2">
            <Properties>
              <Property K="1"/>
              <Property K="2"></Property>
            </Properties>
          </Car>
          <Car V1="3"></Car>
        </Cars>

      assert(f.normalizedEqualsAction(v1, v2))
    }

    test("Equality.NotEquals") {
      val v1 =
        <Cars>
          <Car V1="1"/>
          <Car V1="3"/>
        </Cars>

      val v2 =
        <Cars>
          <Car V1="3" ></Car>
        </Cars>

      assert(!f.normalizedEqualsAction(v1, v2))
    }
  }
}
