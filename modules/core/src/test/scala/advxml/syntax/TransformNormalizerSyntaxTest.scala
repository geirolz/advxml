package advxml.syntax

import advxml.core.transform.XmlNormalizerTest.ContractFuncs
import advxml.core.transform.XmlNormalizerTest
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.Assertion

import scala.xml.NodeSeq

/** Advxml Created by geirolad on 19/07/2019.
  *
  * @author
  *   geirolad
  */
class TransformNormalizerSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.transform._

  XmlNormalizerTest
    .Contract(
      "Syntax", {
        // format: off
        ContractFuncs(
          normalize         = _.normalize,
          normalizedEquals  = (v1, v2) => v1.normalizedEquals(v2)
        )
        // format: on
      }
    )
    .runAll()

  XmlNormalizerTest
    .Contract(
      "Syntax.Symbols", {
        // format: off
        ContractFuncs(
          normalize         = _.normalize,
          normalizedEquals  = (v1, v2) => v1 |==| v2
        )
        // format: on
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
