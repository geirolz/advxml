package advxml.core.data

import advxml.syntax.KeyStringInterpolationOps
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Text

class KeyValueTest extends AnyFunSuite {

  test("Key.==") {
    val key = k"KEY1"
    assert(key == "KEY1")
    assert(!(key == "KEY2"))
  }

  test("Key equals") {
    val key = k"KEY1"
    assert(key != "KEY2")
    assert(key == "KEY1")
  }

  test("AttributeData.toString") {
    val data = AttributeData(k"K", Text("Text"))
    assert(data.toString == s"""Key(K) = "Text"""")
  }

  test("AttributeData.fromMap") {
    val data: List[AttributeData] = AttributeData.fromMap(
      Map(
        "T1" -> "1",
        "T2" -> "2",
        "T3" -> "3"
      )
    )

    assert(
      data == List(
        AttributeData(Key("T1"), Text("1")),
        AttributeData(Key("T2"), Text("2")),
        AttributeData(Key("T3"), Text("3"))
      )
    )
  }

  test("AttributeData.fromElem") {
    val data: List[AttributeData] = AttributeData.fromElem(<foo T1="1" T2="2" T3="3"/>)

    assert(
      data == List(
        AttributeData(Key("T1"), Text("1")),
        AttributeData(Key("T2"), Text("2")),
        AttributeData(Key("T3"), Text("3"))
      )
    )
  }

  test("KeyValuePredicate.toString") {
    val p = KeyValuePredicate(
      k"K",
      new (String => Boolean) {
        override def apply(value: String): Boolean = value == "TEST"
        override def toString(): String = "== [TEST]"
      }
    )

    assert(p.toString == "Key(K) has value == [TEST]")
  }
}
