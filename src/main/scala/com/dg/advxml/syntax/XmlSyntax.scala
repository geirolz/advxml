package com.dg.advxml.syntax

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
trait XmlSyntax {

  implicit class XmlSyntaxOps(xml: NodeSeq) {
    def \@!(name: String): Try[String] = {
      xml \@ name match {
        case value if value.isEmpty => Failure(new RuntimeException(s"Missing attribute: $name"))
        case value => Success(value)
      }
    }

    def \@?(name: String): Option[String] = {
      xml \@ name match {
        case value if value.isEmpty => None
        case value => Some(value)
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
