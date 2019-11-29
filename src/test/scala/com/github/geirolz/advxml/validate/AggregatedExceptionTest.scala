package com.github.geirolz.advxml.validate

import com.github.geirolz.advxml.validate.exceptions.AggregatedException
import org.scalatest.funsuite.AnyFunSuite

/**
  * Advxml
  * Created by geirolad on 29/07/2019.
  *
  * @author geirolad
  */
class AggregatedExceptionTest extends AnyFunSuite {

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
