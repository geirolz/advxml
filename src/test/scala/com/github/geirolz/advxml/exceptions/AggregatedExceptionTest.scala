package com.github.geirolz.advxml.exceptions

import org.scalatest.FunSuite

/**
  * Advxml
  * Created by geirolad on 29/07/2019.
  *
  * @author geirolad
  */
class AggregatedExceptionTest extends FunSuite {

  test("Test Aggregated exception") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    ex.printStackTrace()
  }

  test("Test Aggregated exception - getStackTraces") {
    val ex = new AggregatedException(
      Seq(
        new RuntimeException("EX1"),
        new RuntimeException("EX2"),
        new RuntimeException("EX3")
      )
    )

    val result = ex.getStackTraces
    assert(result.size == 3)
  }
}
