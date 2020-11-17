package advxml.core.data

import scala.xml.Text

case class Key(value: String) extends AnyVal

trait KeyValue[T] {
  val key: Key
  val value: T
}

case class KeyValuePredicate[T](key: Key, valuePredicate: T => Boolean) {
  lazy val negate: KeyValuePredicate[T] = copy(valuePredicate = t => !valuePredicate(t))
}

//###########################################################################
case class AttributeData(key: Key, value: Text) extends KeyValue[Text]
