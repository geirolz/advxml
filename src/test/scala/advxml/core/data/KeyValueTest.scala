package advxml.core.data

import advxml.syntax.KeyStringInterpolationOps
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Text

class KeyValueTest extends AnyFunSuite {

  test("AttributeData.toString") {
    val data = AttributeData(k"K", Text("Text"))
    assert(data.toString == s"""Key(K) = "Text"""")
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
