package com.github.geirolz.advxml.convert

import cats.data.Validated.Valid
import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes
import com.github.geirolz.advxml.convert.XmlConverter.{ModelToXml, XmlToModel}
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

  test("XML to Model - Convert simple case class") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: XmlToModel[Elem, Person] = x => {
      (
        x.\@!("Name").toValidatedNel,
        x.\@!("Surname").toValidatedNel,
        x.\@?("Age").map(_.toInt).validNel
      ).mapN(Person)
    }

    val xml = <Person Name="Matteo" Surname="Bianchi"/>
    val res: ValidatedRes[Person] = xml.as[Person]

    assert(res.isValid)
    assert(res.toOption.get.name == "Matteo")
    assert(res.toOption.get.surname == "Bianchi")
  }

  test("Model to XML - Convert simple case class") {

    case class Person(name: String, surname: String, age: Option[Int])

    implicit val converter: ModelToXml[Person, Elem] = x =>
      Valid {
        <Person Name={x.name} Surname={x.surname} Age={x.age.map(_.toString).getOrElse("")}/>
      }

    val p = Person("Matteo", "Bianchi", Some(23))
    val res: ValidatedRes[Elem] = p.asXml

    assert(res.isValid)
    assert(res.toOption.get == <Person Name="Matteo" Surname="Bianchi" Age="23"/>)
  }
}
