package advxml.syntax

import advxml.core.XmlNormalizerTest
import advxml.core.XmlNormalizerTest.ContractFuncs
import advxml.test.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.Assertion

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 19/07/2019.
  *
  * @author geirolad
  */
class XmlNormalizerSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.normalize._

  XmlNormalizerTest
    .Contract(
      "Syntax", {
        ContractFuncs(
          normalizeAction = _.normalize,
          normalizedEqualsAction = (v1, v2) => v1.normalizedEquals(v2)
        )
      }
    )
    .runAll()

  XmlNormalizerTest
    .Contract(
      "Syntax.Symbols", {
        ContractFuncs(
          normalizeAction = _.normalize,
          normalizedEqualsAction = (v1, v2) => v1 === v2
        )
      }
    )
    .runAll()

  test("[XmlNormalizer.Syntax.Symbols] - Equality.NotEqualsMethod") {
    assertEqualityNotEquals((v1, v2) => v1 |!=| v2)
  }

  test("[XmlNormalizer.Syntax.Symbols] - Equality.NotEqualsMethod[Scalatric]") {
    assertEqualityNotEquals((v1, v2) => v1 !== v2)
  }

  private def assertEqualityNotEquals(p: (NodeSeq, NodeSeq) => Boolean): Assertion = {
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
