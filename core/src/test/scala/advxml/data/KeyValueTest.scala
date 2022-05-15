package advxml.data

import cats.Eq
import org.scalatest.funsuite.AnyFunSuite

class KeyValueTest extends AnyFunSuite {

  import advxml.implicits.*

  test("Key.==") {
    val key: Key = k"KEY1"
    assert(key == "KEY1")
    assert(!(key == "KEY2"))
  }

  test("Key equals") {
    val key: Key = k"KEY1"
    assert(key != "KEY2")
    assert(key == "KEY1")
  }

  test("Key.Eq for Cats") {
    import advxml.data.Key.*
    assert(Eq[Key].eqv(k"KEY1", k"KEY1"))
    assert(Eq[Key].neqv(k"KEY1", k"KEY2"))
  }

  test("AttributeData.toString") {
    val data: AttributeData = AttributeData(k"K", v"Text")
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
        AttributeData(k"T1", v"1"),
        AttributeData(k"T2", v"2"),
        AttributeData(k"T3", v"3")
      )
    )
  }

  test("AttributeData.fromElem") {
    val data: List[AttributeData] = AttributeData.fromElem(<foo T1="1" T2="2" T3="3"/>)

    assert(
      data == List(
        AttributeData(Key("T1"), v"1"),
        AttributeData(Key("T2"), v"2"),
        AttributeData(Key("T3"), v"3")
      )
    )
  }

  test("KeyValuePredicate.toString") {
    val p = KeyValuePredicate(
      k"K",
      new SimpleValue => Boolean {
        override def apply(value: SimpleValue): Boolean = value.get == "TEST"
        override def toString(): String                 = "== [TEST]"
      }
    )

    assert(p.toString == "Key(K) has value == [TEST]")
  }
}
