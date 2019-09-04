package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.ValidatedRes.ValidatedRes

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
object XmlTraverser {

  import cats.implicits._

  object mandatory {

    def immediateChildren(ns: NodeSeq, name: String): ValidatedRes[NodeSeq] = {
      ns \ name match {
        case value if value.isEmpty => new RuntimeException(s"Missing node: $name").invalidNel
        case value                  => value.validNel
      }
    }

    def children(ns: NodeSeq, name: String): ValidatedRes[NodeSeq] = {
      ns \\ name match {
        case value if value.isEmpty => new RuntimeException(s"Missing nested node: $name").invalidNel
        case value                  => value.validNel
      }
    }

    def attr(ns: NodeSeq, name: String): ValidatedRes[String] = {
      ns \@ name match {
        case value if value.isEmpty => new RuntimeException(s"Missing attribute: $name").invalidNel
        case value                  => value.validNel
      }
    }

    def text(ns: NodeSeq): ValidatedRes[String] = {
      ns.text match {
        case value if value.isEmpty => new RuntimeException("Missing text").invalidNel
        case value                  => value.validNel
      }
    }
  }

  object optional {

    def immediateChildren(ns: NodeSeq, name: String): ValidatedRes[Option[NodeSeq]] =
      mandatory.immediateChildren(ns, name).toOption.validNel

    def children(ns: NodeSeq, name: String): ValidatedRes[Option[NodeSeq]] =
      mandatory.children(ns, name).toOption.validNel

    def attr(ns: NodeSeq, name: String): ValidatedRes[Option[String]] =
      mandatory.attr(ns, name).toOption.validNel

    def text(ns: NodeSeq): ValidatedRes[Option[String]] =
      mandatory.text(ns).toOption.validNel
  }
}
