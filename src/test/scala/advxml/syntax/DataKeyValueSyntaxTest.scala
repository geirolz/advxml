package advxml.syntax

import advxml.core.data.{AttributeData, Key, KeyValuePredicate, Value}
import advxml.syntax.DataKeyValueSyntaxTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import org.scalatest.funsuite.AnyFunSuite

class DataKeyValueSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.data._
  import advxml.instances.data.convert._

  DataKeyValueSyntaxTest
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

object DataKeyValueSyntaxTest {

  case class ContractFuncs(
    data: (Key, Value) => AttributeData,
    withPredicate: (Key, Value => Boolean) => KeyValuePredicate,
    equals: (Key, Double) => KeyValuePredicate,
    notEquals: (Key, Double) => KeyValuePredicate,
    lessThen: (Key, Double) => KeyValuePredicate,
    lessEqThen: (Key, Double) => KeyValuePredicate,
    greaterThen: (Key, Double) => KeyValuePredicate,
    greaterEqThen: (Key, Double) => KeyValuePredicate
  )

  case class Contract(subDesc: String = "", f: ContractFuncs) extends ContractTests("KeyValue", subDesc) {

    private val key: Key = Key("key")

    test("data") {
      val value: Value = Value("TEST")
      val attrData: AttributeData = f.data(key, value)

      assert(attrData.key == key)
      assert(attrData.value == value)
    }

    test("filter") {
      val p: KeyValuePredicate = f.withPredicate(key, _ == Value("1"))

      assert(p(Value("1")))
      assert(!p(Value("2")))
    }

    test("equals") {
      val p: KeyValuePredicate = f.equals(key, 1d)

      assert(p(Value("1")))
      assert(!p(Value("0.5")))
      assert(!p(Value("2")))
    }

    test("notEquals") {
      val p: KeyValuePredicate = f.notEquals(key, 1d)

      assert(p(Value("2")))
      assert(!p(Value("1")))
    }

    test("lessThen") {
      val p: KeyValuePredicate = f.lessThen(key, 1d)

      assert(p(Value("0.5")))
      assert(!p(Value("1")))
      assert(!p(Value("2")))
    }

    test("lessEqThen") {
      val p: KeyValuePredicate = f.lessEqThen(key, 1d)

      assert(p(Value("0.5")))
      assert(p(Value("1")))
      assert(!p(Value("2")))
    }

    test("greaterThen") {
      val p: KeyValuePredicate = f.greaterThen(key, 1d)

      assert(p(Value("2")))
      assert(!p(Value("1")))
      assert(!p(Value("0.5")))
    }

    test("greaterEqThen") {
      val p: KeyValuePredicate = f.greaterEqThen(key, 1d)

      assert(p(Value("2")))
      assert(p(Value("1")))
      assert(!p(Value("0.5")))
    }
  }
}
