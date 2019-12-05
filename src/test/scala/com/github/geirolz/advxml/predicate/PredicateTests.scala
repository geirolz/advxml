package com.github.geirolz.advxml.predicate

import org.scalatest.FunSuite

class PredicateTests extends FunSuite {

  import com.github.geirolz.advxml.implicits.predicate._

  test("Combine two predicate with in and") {
    testAnd((p1, p2) => p1.and(p2))
  }

  test("Combine two predicate with in or") {
    testOr((p1, p2) => p1.or(p2))
  }

  test("Combine two predicate with in and - symbol") {
    testAnd((p1, p2) => p1 && p2)
  }

  test("Combine two predicate with in or - symbol") {
    testOr((p1, p2) => p1 || p2)
  }

  private def testAnd(f: (String => Boolean, String => Boolean) => String => Boolean) = {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = f(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AC"))
    assert(!p3("AB"))
    assert(!p3("GF"))
  }

  private def testOr(f: (String => Boolean, String => Boolean) => String => Boolean) = {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = f(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AB"))
    assert(!p3("GF"))
  }
}
