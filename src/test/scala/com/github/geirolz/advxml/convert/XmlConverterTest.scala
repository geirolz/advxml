package com.github.geirolz.advxml.convert

import com.github.geirolz.advxml.convert.Validation.ValidationRes
import com.github.geirolz.advxml.convert.XmlConverter.XmlToModel
import com.github.geirolz.advxml.traverse.XmlTraverser
import org.scalatest.FunSuite

import scala.language.postfixOps
import scala.xml.Elem

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
class XmlConverterTest extends FunSuite {

  import Validation.ops._
  import XmlConverter.ops._
  import XmlTraverser.ops._

  test("Convert") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: XmlToModel[Elem, Person] = x => (
      x \@! "Name" toValidatedNel,
      x \@! "Surname" toValidatedNel,
      (x \@? "Age").map(_.toInt).validNel[Throwable]
    ).mapN(Person)

    val xml = <Person Name="Matteo" Surname="Bianchi"/>
    val res: ValidationRes[Person] = xml.as[Person]


    assert(res.isValid)
    assert(res.toOption.get.name == "Matteo")
    assert(res.toOption.get.surname == "Bianchi")
  }
}
