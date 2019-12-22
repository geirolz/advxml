package advxml.core

import org.scalatest.FunSuite

class PredicateTests extends FunSuite with PredicateAsserts {

  test("Combine two predicate with in and") {
    testAnd((p1, p2) => Predicate.and(p1, p2))
  }

  test("Combine two predicate with in or") {
    testOr((p1, p2) => Predicate.or(p1, p2))
  }
}

private[advxml] trait PredicateAsserts {

  def testAnd(f: (String => Boolean, String => Boolean) => String => Boolean): Unit = {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = f(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AC"))
    assert(!p3("AB"))
    assert(!p3("GF"))
  }

  def testOr(f: (String => Boolean, String => Boolean) => String => Boolean): Unit = {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = f(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AB"))
    assert(!p3("GF"))
  }
}
