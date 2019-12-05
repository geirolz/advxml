package advxml.syntax

import advxml.core.PredicateAsserts
import org.scalatest.FunSuite

class PredicateSyntaxTest extends FunSuite with PredicateAsserts {

  import advxml.syntax.predicate._

  test("Combine two predicate with in and - && syntax") {
    testAnd((p1, p2) => p1 && p2)
  }

  test("Combine two predicate with in or - || syntax") {
    testOr((p1, p2) => p1 || p2)
  }

  test("Combine two predicate with in and - and syntax") {
    testAnd((p1, p2) => p1.and(p2))
  }

  test("Combine two predicate with in or - or syntax") {
    testOr((p1, p2) => p1.or(p2))
  }
}
