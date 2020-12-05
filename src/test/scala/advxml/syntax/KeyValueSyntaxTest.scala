package advxml.syntax

import advxml.core.data.{AttributeData, Key, KeyValuePredicate}
import advxml.syntax.KeyValueSyntaxTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

class KeyValueSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.instances.convert._

  KeyValueSyntaxTest
    .Contract(
      // format: off
      f = ContractFuncs(
        data          = _ := _,
        withPredicate = _ -> _,
        equals        = _ === _,
        notEquals     = _ =!= _,
        lessThen      = _ < _,
        lessEqThen    = _ <= _,
        greaterThen   = _ > _,
        greaterEqThen = _ >= _
      )
      // format: on
    )
    .runAll()
}

object KeyValueSyntaxTest {

  case class ContractFuncs(
    data: (Key, String) => AttributeData,
    withPredicate: (Key, String => Boolean) => KeyValuePredicate[String],
    equals: (Key, Double) => KeyValuePredicate[String],
    notEquals: (Key, Double) => KeyValuePredicate[String],
    lessThen: (Key, Double) => KeyValuePredicate[String],
    lessEqThen: (Key, Double) => KeyValuePredicate[String],
    greaterThen: (Key, Double) => KeyValuePredicate[String],
    greaterEqThen: (Key, Double) => KeyValuePredicate[String]
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("KeyValue", subDesc) {

    private val key: Key = Key("key")

    test("data") {
      val text: String = "TEST"
      val attrData: AttributeData = f.data(key, text)

      assert(attrData.key == key)
      assert(attrData.value == text)
    }

    test("filter") {
      val p: KeyValuePredicate[String] = f.withPredicate(key, _ == "1")

      assert(p("1"))
      assert(!p("2"))
    }

    test("equals") {
      val p: KeyValuePredicate[String] = f.equals(key, 1d)

      assert(p("1"))
      assert(!p("0.5"))
      assert(!p("2"))
    }

    test("notEquals") {
      val p: KeyValuePredicate[String] = f.notEquals(key, 1d)

      assert(p("2"))
      assert(!p("1"))
    }

    test("lessThen") {
      val p: KeyValuePredicate[String] = f.lessThen(key, 1d)

      assert(p("0.5"))
      assert(!p("1"))
      assert(!p("2"))
    }

    test("lessEqThen") {
      val p: KeyValuePredicate[String] = f.lessEqThen(key, 1d)

      assert(p("0.5"))
      assert(p("1"))
      assert(!p("2"))
    }

    test("greaterThen") {
      val p: KeyValuePredicate[String] = f.greaterThen(key, 1d)

      assert(p("2"))
      assert(!p("1"))
      assert(!p("0.5"))
    }

    test("greaterEqThen") {
      val p: KeyValuePredicate[String] = f.greaterEqThen(key, 1d)

      assert(p("2"))
      assert(p("1"))
      assert(!p("0.5"))
    }
  }
}
