package advxml.instances

import advxml.core.data.Value
import org.scalatest.funsuite.AnyFunSuite

import scala.util.matching.Regex

class DataValueValidationRuleInstancesTest extends AnyFunSuite {

  import advxml.instances.data.value._

  test("ValidationRule.NonEmpty") {
    assert(NonEmpty(Value("TEST")).isValid)
    assert(NonEmpty(Value("")).isInvalid)
  }

  test("ValidationRule.MatchRegex") {
    val onlyNumbers: Regex = "^[0-9]*$".r
    assert(MatchRegex(onlyNumbers)(Value("123456")).isValid)
    assert(MatchRegex(onlyNumbers)(Value("TEST1234")).isInvalid)
  }
}
