package com.github.geirolz.advxml.utils

import org.scalatest.FunSuite

class PredicateUtilsTests extends FunSuite {

  test("Combine two predicate with in and") {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = PredicateUtils.and(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AC"))
    assert(!p3("AB"))
    assert(!p3("GF"))
  }

  test("Combine two predicate with in or") {
    val p1: String => Boolean = _.contains("A")
    val p2: String => Boolean = _.contains("C")
    val p3 = PredicateUtils.or(p1, p2)

    assert(p1("A"))
    assert(p2("C"))
    assert(p3("AB"))
    assert(!p3("GF"))
  }
}
