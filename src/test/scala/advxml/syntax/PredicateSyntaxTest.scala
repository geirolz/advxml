package advxml.syntax

import advxml.core.PredicateTests
import advxml.test.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

class PredicateSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.predicate._

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
