package com.dg.advxml.syntax

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
private [advxml] trait XmlSyntax {

  implicit class XmlSyntaxOps(xml: NodeSeq) {

    def \?(name: String) : Option[NodeSeq] = (xml \! name).toOption

    def \!(name: String) : Try[NodeSeq] = {
      xml \ name match {
        case value if value.isEmpty => Failure(new RuntimeException(s"Missing node: $name"))
        case value => Success(value)
      }
    }


    def \\?(name: String) : Option[NodeSeq] = (xml \\! name).toOption

    def \\!(name: String) : Try[NodeSeq] = {
      xml \\ name match {
        case value if value.isEmpty => Failure(new RuntimeException(s"Missing nested node: $name"))
        case value => Success(value)
      }
    }


    def \@?(name: String): Option[String] = (xml \@! name).toOption

    def \@!(name: String): Try[String] = {
      xml \@ name match {
        case value if value.isEmpty => Failure(new RuntimeException(s"Missing attribute: $name"))
        case value => Success(value)
      }
    }


    def content: Try[String] = {
      xml.text match {
        case value if value.isEmpty => Failure(new RuntimeException("Missing content"))
        case value => Success(value)
      }
    }
  }
}

object XmlSyntax extends XmlSyntax