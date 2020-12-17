package advxml.core.data

import cats.{Eq, Show}

import scala.xml.Elem

case class Key(value: String) extends AnyVal with Serializable {
  def ==(that: String): Boolean = value == that
  def !=(that: String): Boolean = ! ==(that)
}
object Key {
  implicit val advxmlKeyCatsInstances: Eq[Key] with Show[Key] = new Eq[Key] with Show[Key] {
    override def eqv(x: Key, y: Key): Boolean = x == y.value
    override def show(t: Key): String = t.toString
  }
}

case class KeyValuePredicate(key: Key, private val valuePredicate: Value => Boolean) {

  def apply(t: Value): Boolean = valuePredicate(t)

  lazy val negate: KeyValuePredicate = copy(valuePredicate = t => !valuePredicate(t))

  override def toString: String = s"$key has value $valuePredicate"
}

case class AttributeData(key: Key, value: Value) {
  override def toString: String = s"""$key = $value"""
}

object AttributeData {

  def fromMap(m: Map[String, String]): List[AttributeData] =
    m.map { case (k, v) => AttributeData(Key(k), Value(v)) }.toList

  def fromElem(e: Elem): List[AttributeData] =
    fromMap(e.attributes.asAttrMap)
}
