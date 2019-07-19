package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.convert.XmlConverter.XmlToModel
import org.scalatest.FunSuite

import scala.xml.Elem

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
class XmlConverterTest extends FunSuite {

  import cats.implicits._
  import com.github.geirolz.advxml.implicits.converter._
  import com.github.geirolz.advxml.implicits.traverser._
  import com.github.geirolz.advxml.implicits.validation._

  test("Convert simple case class") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: XmlToModel[Elem, Person] = x =>
      (
        x \@! "Name",
        x \@! "Surname",
        x \@? "Age" mapValue (_.toInt)
      ).mapN(Person)

    val xml = <Person Name="Matteo" Surname="Bianchi"/>
    val res: ValidatedRes[Person] = xml.as[Person]

    assert(res.isValid)
    assert(res.toOption.get.name == "Matteo")
    assert(res.toOption.get.surname == "Bianchi")
  }
}
