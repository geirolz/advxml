package advxml.core.data

import scala.xml.Text

case class Key(value: String) extends AnyVal

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
case class AttributeData(key: Key, value: Text) extends KeyValue[Text] {
  override def toString: String = s"""$key = "${value.text}""""
}
