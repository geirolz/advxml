package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.XmlTextSerializer.StrSerializer

import scala.xml.Text

/**
  * Adxml
  * Created by geirolad on 03/07/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTextSerializerSyntax {

  implicit class OptionOps[T](t: Option[T]) {
    def asText(implicit s: StrSerializer[T]): Option[Text] = XmlTextSerializer.asText(t)
  }

  implicit class AnyValOps[T](t: T) {
    def asText(implicit s: StrSerializer[T]): Text = XmlTextSerializer.asText(t)
  }

}

private[advxml] trait XmlTextSerializerInstances {
  implicit val serializable: StrSerializer[Serializable] = _.toString
  implicit val byte: StrSerializer[Byte] = _.toString
  implicit val short: StrSerializer[Short] = _.toString
  implicit val char: StrSerializer[Char] = _.toString
  implicit val int: StrSerializer[Int] = _.toString
  implicit val long: StrSerializer[Long] = _.toString
  implicit val float: StrSerializer[Float] = _.toString
  implicit val double: StrSerializer[Double] = _.toString
}

object XmlTextSerializer {

  type StrSerializer[T] = T => String

  def asText[T](t: T)(implicit s: StrSerializer[T]): Text = Text(s(t))

  def asText[T](t: Option[T])(implicit s: StrSerializer[T]): Option[Text] = t.map(asText(_))

  object ops extends XmlTextSerializerSyntax

  object instances extends XmlTextSerializerInstances

}



