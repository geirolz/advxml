package advxml.core.data

import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.util.matching.Regex

class ValueTest extends AnyFunSuite {

  import advxml.instances.data.value._

  test("Value.unboxed") {
    val str = "TEST"
    val value: Value = Value(str)
    assert(value.unboxed == str)
  }

  test("Value.extract") {
    val str = "TEST"
    val value: Value = Value(str)
    assert(value.extract[Try] == Success(str))
  }

  test("Value.compareTo") {
    val v1: Value = Value("1")
    val v2: Value = Value("1")
    val v3: Value = Value("0")

    assert(v1.compareTo(v2) == 0)
    assert(v1.compareTo(v3) > 0)
    assert(v3.compareTo(v1) < 0)
  }

  test("Value.validate") {
    val value: ValidatedValue = Value("TEST").validate(NonEmpty)
    assert(value.unboxed == "TEST")
    assert(value.rules == NonEmptyList.one(NonEmpty))
  }

  test("Value.nonEmpty") {
    val value = Value("TEST").nonEmpty
    assert(value.unboxed == "TEST")
    assert(value.rules == NonEmptyList.one(NonEmpty))
  }

  test("Value.matchRegex") {
    val regex: Regex = "@\"^\\d$\"".r()
    val value = Value("TEST").matchRegex(regex)
    assert(value.unboxed == "TEST")
    assert(value.rules == NonEmptyList.one(MatchRegex(regex)))
  }

  test("Value.toString") {
    val value: Value = Value("TEST")
    val valueWithRef: Value = Value("TEST", Some("REF"))

    assert(value.toString == """"TEST"""")
    assert(valueWithRef.toString == """REF => "TEST"""")
  }
}

class ValidatedValueTest extends AnyFunSuite {

  test("Value.extract") {
    val str = "TEST"
    val value: Value = Value(str).nonEmpty
    val emptyValue: Value = Value("").nonEmpty

    assert(value.extract[Try] == Success(str))
    assert(emptyValue.extract[Try].isFailure)
  }

  test("ValidatedValue.extract") {
    val str = "TEST"
    val value: ValidatedValue = Value(str).nonEmpty
    val emptyValue: ValidatedValue = Value("").nonEmpty

    assert(value.extract[Try] == Success(str))
    assert(emptyValue.extract[Try].isFailure)
  }

  test("ValidatedValue.validate") {
    val customRule = ValidationRule("ContainsOne", _.contains("1"), "Value doesn't contains '1'")
    val value: ValidatedValue = Value("TEST").nonEmpty.validate(customRule)
    assert(value.extract[Try].isFailure)
  }

  test("ValidatedValue.toValue") {
    val value: Value = Value("").nonEmpty.toValue
    assert(value.extract[Try] == Success(""))
  }

}
