package advxml.syntax

import advxml.core.data.PredicateTests
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

class DataPredicateSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.data._

  PredicateTests
    .Contract(
      "Syntax", {
        PredicateTests.ContractFuncs(
          and = (p1, p2) => p1.and(p2),
          or = (p1, p2) => p1.or(p2)
        )
      }
    )
    .runAll()

  PredicateTests
    .Contract(
      "Syntax.Symbols", {
        PredicateTests.ContractFuncs(
          and = (p1, p2) => p1 && p2,
          or = (p1, p2) => p1 || p2
        )
      }
    )
    .runAll()
}
