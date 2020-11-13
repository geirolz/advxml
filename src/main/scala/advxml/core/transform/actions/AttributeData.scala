package advxml.core.transform.actions

import scala.xml.Text

case class Key(value: String)

sealed trait KeyValue[T] {
  val key: Key
  val value: T
}

case class KeyValuePredicate[T](key: Key, valuePredicate: T => Boolean) {
  lazy val negate: KeyValuePredicate[T] = copy(valuePredicate = t => !valuePredicate(t))
}

//********************************* CUSTOMIZATIONS *************************************
case class AttributeData(key: Key, value: Text) extends KeyValue[Text]
