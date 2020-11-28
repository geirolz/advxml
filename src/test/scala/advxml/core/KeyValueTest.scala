package advxml.core

import advxml.core.data.{Key, KeyValuePredicate}
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

class KeyValueTest extends AnyFunSuite with FunSuiteContract {
  //
  //  KeyValueTest
  //    .Contract(
  //      // format: off
  //      f = ContractFuncs(
  //        lessThen = (k, v) => k < v
  //      )
  //      // format: on
  //    )
  //    .runAll()
}

object KeyValueTest {

  case class ContractFuncs(
    //    data: (Key, Text) => AttributeData,
    //    filter: (Key, Text => Boolean) => KeyValuePredicate[Text],
    equals: (Key, Double) => KeyValuePredicate[String],
    notEquals: (Key, Double) => KeyValuePredicate[String],
    lessThen: (Key, Double) => KeyValuePredicate[String],
    lessEqThen: (Key, Double) => KeyValuePredicate[String],
    greaterThen: (Key, Double) => KeyValuePredicate[String],
    greaterEqThen: (Key, Double) => KeyValuePredicate[String]
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("KeyValue", subDesc) {

    private val key: Key = Key("key")

    test("equals") {
      val result: KeyValuePredicate[String] = f.equals(key, 1d)

      assert(result.valuePredicate("1"))
      assert(!result.valuePredicate("0.5"))
      assert(!result.valuePredicate("2"))
    }

    test("notEquals") {
      val result: KeyValuePredicate[String] = f.notEquals(key, 1d)

      assert(result.valuePredicate("2"))
      assert(!result.valuePredicate("1"))
    }

    test("lessThen") {
      val result: KeyValuePredicate[String] = f.lessThen(key, 1d)

      assert(result.valuePredicate("0.5"))
      assert(!result.valuePredicate("1"))
      assert(!result.valuePredicate("2"))
    }

    test("lessEqThen") {
      val result: KeyValuePredicate[String] = f.lessEqThen(key, 1d)

      assert(result.valuePredicate("0.5"))
      assert(result.valuePredicate("1"))
      assert(!result.valuePredicate("2"))
    }

    test("greaterThen") {
      val result: KeyValuePredicate[String] = f.greaterThen(key, 1d)

      assert(result.valuePredicate("2"))
      assert(!result.valuePredicate("1"))
      assert(!result.valuePredicate("0.5"))
    }

    test("greaterEqThen") {
      val result: KeyValuePredicate[String] = f.greaterEqThen(key, 1d)

      assert(result.valuePredicate("2"))
      assert(result.valuePredicate("1"))
      assert(!result.valuePredicate("0.5"))
    }
  }
}
