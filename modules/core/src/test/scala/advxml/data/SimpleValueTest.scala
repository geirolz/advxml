package advxml.data

import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.util.matching.Regex

class SimpleValueTest extends AnyFunSuite {

  test("Value.get") {
    val str                = "TEST"
    val value: SimpleValue = SimpleValue(str)
    assert(value.get == str)
  }

  test("Value.compareTo") {
    val v1: SimpleValue = SimpleValue("1")
    val v2: SimpleValue = SimpleValue("1")
    val v3: SimpleValue = SimpleValue("0")

    assert(v1.compareTo(v2) == 0)
    assert(v1.compareTo(v3) > 0)
    assert(v3.compareTo(v1) < 0)
  }

  test("Value.validate") {
    val value: ValidatedValue = SimpleValue("TEST").validate(ValidationRule.NonEmpty)
    assert(value.rules == NonEmptyList.one(ValidationRule.NonEmpty))
  }

  test("Value.nonEmpty") {
    val value: ValidatedValue = SimpleValue("TEST").nonEmpty
    assert(value.rules == NonEmptyList.one(ValidationRule.NonEmpty))
  }

  test("Value.matchRegex") {
    val regex: Regex          = new Regex("@\"^\\d$\"")
    val value: ValidatedValue = SimpleValue("TEST").matchRegex(regex)
    assert(value.rules == NonEmptyList.one(ValidationRule.MatchRegex(regex)))
  }

  test("Value.toString") {
    val value: SimpleValue        = SimpleValue("TEST")
    val valueWithRef: SimpleValue = SimpleValue("TEST", Some("REF"))

    assert(value.toString == """"TEST"""")
    assert(valueWithRef.toString == """REF => "TEST"""")
  }
}

class ValidatedValueTest extends AnyFunSuite {

  test("ValidatedValue.extract") {
    val str                        = "TEST"
    val value: ValidatedValue      = SimpleValue(str).nonEmpty
    val emptyValue: ValidatedValue = SimpleValue("").nonEmpty

    assert(value.extract[Try] == Success(str))
    assert(emptyValue.extract[Try].isFailure)
  }

  test("ValidatedValue.validate") {
    val customRule = ValidationRule("ContainsOne")(_.contains("1"), "Value doesn't contains '1'")
    val value: ValidatedValue = SimpleValue("TEST").nonEmpty.validate(customRule)
    assert(value.extract[Try].isFailure)
  }

  test("ValidatedValue.toValue") {
    val value: SimpleValue = SimpleValue("").nonEmpty.toSimpleValue
    assert(value.get == "")
  }
}
