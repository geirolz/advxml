package advxml.core.data

import advxml.core.data.Predicate.{alwaysFalse, alwaysTrue}
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

class PredicateTests extends AnyFunSuite with FunSuiteContract {
  PredicateTests
    .Contract(
      f = PredicateTests.ContractFuncs(
        and = (p1, p2) => Predicate.and(p1, p2),
        or  = (p1, p2) => Predicate.or(p1, p2)
      )
    )
    .runAll()

  test("Test 'alwaysTrue' predicate") {
    assert(alwaysTrue(10))
    assert(alwaysTrue("10"))
    assert(alwaysTrue(null))
  }

  test("Test 'alwaysFalse' predicate") {
    assert(!alwaysFalse(10))
    assert(!alwaysFalse("10"))
    assert(!alwaysFalse(null))
  }
}

object PredicateTests {

  case class ContractFuncs(
    and: (String => Boolean, String => Boolean) => String => Boolean,
    or: (String => Boolean, String => Boolean) => String => Boolean
  )

  case class Contract(subDesc: String = "", f: ContractFuncs)
      extends ContractTests("Predicate", subDesc) {

    test("And") {
      val p1: String => Boolean = _.contains("A")
      val p2: String => Boolean = _.contains("C")
      val p3                    = f.and(p1, p2)

      assert(p1("A"))
      assert(p2("C"))
      assert(p3("AC"))
      assert(!p3("AB"))
      assert(!p3("GF"))
    }

    test("Or") {
      val p1: String => Boolean = _.contains("A")
      val p2: String => Boolean = _.contains("C")
      val p3                    = f.or(p1, p2)

      assert(p1("A"))
      assert(p2("C"))
      assert(p3("AB"))
      assert(!p3("GF"))
    }
  }
}
