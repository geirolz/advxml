package advxml.data

import org.scalatest.funsuite.AnyFunSuite

import scala.util.matching.Regex

class DataValueValidationRuleInstancesTest extends AnyFunSuite {

  test("ValidationRule.NonEmpty") {
    assert(ValidationRule.NonEmpty(SimpleValue("TEST")).isValid)
    assert(ValidationRule.NonEmpty(SimpleValue("")).isInvalid)
  }

  test("ValidationRule.MatchRegex") {
    val onlyNumbers: Regex = "^[0-9]*$".r
    assert(ValidationRule.MatchRegex(onlyNumbers)(SimpleValue("123456")).isValid)
    assert(ValidationRule.MatchRegex(onlyNumbers)(SimpleValue("TEST1234")).isInvalid)
  }
}
