package advxml.instances

import advxml.core.data.SimpleValue
import org.scalatest.funsuite.AnyFunSuite

import scala.util.matching.Regex

class DataValueValidationRuleInstancesTest extends AnyFunSuite {

  import advxml.instances.data.value._

  test("ValidationRule.NonEmpty") {
    assert(NonEmpty(SimpleValue("TEST")).isValid)
    assert(NonEmpty(SimpleValue("")).isInvalid)
  }

  test("ValidationRule.MatchRegex") {
    val onlyNumbers: Regex = "^[0-9]*$".r
    assert(MatchRegex(onlyNumbers)(SimpleValue("123456")).isValid)
    assert(MatchRegex(onlyNumbers)(SimpleValue("TEST1234")).isInvalid)
  }
}
