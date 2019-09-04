package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.XmlTextSerializer.Serializer

import scala.math.ScalaNumber
import scala.xml.Text

/**
  * Advxml
  * Created by geirolad on 03/07/2019.
  *
  * @author geirolad
  */
private[advxml] trait XmlTextSerializerSyntax {

  implicit class OptionOps[T](t: Option[T]) {
    def asText(implicit s: Serializer[T]): Option[Text] = XmlTextSerializer.asText(t)
  }

  implicit class AnyValOps[T](t: T) {
    def asText(implicit s: Serializer[T]): Text = XmlTextSerializer.asText(t)
  }
}

private[advxml] trait XmlTextSerializerInstances {
  implicit val scalaNumber: Serializer[ScalaNumber] = _.toString
  implicit val byte: Serializer[Byte] = _.toString
  implicit val short: Serializer[Short] = _.toString
  implicit val char: Serializer[Char] = _.toString
  implicit val int: Serializer[Int] = _.toString
  implicit val long: Serializer[Long] = _.toString
  implicit val float: Serializer[Float] = _.toString
  implicit val double: Serializer[Double] = _.toString
}

object XmlTextSerializer {

  type Serializer[-T] = T => String

  def asText[T](t: Option[T])(implicit s: Serializer[T]): Option[Text] = t.map(asText(_))

  def asText[T](t: T)(implicit s: Serializer[T]): Text = Text(s(t))
}
