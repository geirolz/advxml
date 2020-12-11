package advxml.core.data

import scala.xml.Elem

//TODO: Scala3 - Opaque Type candidate
case class Key(value: String) extends AnyVal {
  def ==(that: String): Boolean = value == that
  def !=(that: String): Boolean = ! ==(that)
}

trait KeyValue[T] {
  val key: Key
  val value: T
}

case class KeyValuePredicate[T](key: Key, private val valuePredicate: T => Boolean) {

  def apply(t: T): Boolean = valuePredicate(t)

  lazy val negate: KeyValuePredicate[T] = copy(valuePredicate = t => !valuePredicate(t))

  override def toString: String = s"$key has value $valuePredicate"
}

//###########################################################################
case class AttributeData(key: Key, value: String) extends KeyValue[String] {
  override def toString: String = s"""$key = "$value""""
}
object AttributeData {

  def fromMap(m: Map[String, String]): List[AttributeData] =
    m.map { case (k, v) => AttributeData(Key(k), v) }.toList

  def fromElem(e: Elem): List[AttributeData] =
    fromMap(e.attributes.asAttrMap)
}
