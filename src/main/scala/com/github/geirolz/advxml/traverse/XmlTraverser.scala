package com.github.geirolz.advxml.traverse

import com.github.geirolz.advxml.convert.Validation.ValidationRes

import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
object XmlTraverser {

  import cats.implicits._

  def immediateChildren(ns: NodeSeq, name: String) : ValidationRes[Option[NodeSeq]] =
    mandatoryImmediateChildren(ns, name).toOption.validNel

  def mandatoryImmediateChildren(ns: NodeSeq, name: String) : ValidationRes[NodeSeq] = {
    ns \ name match {
      case value if value.isEmpty => new RuntimeException(s"Missing node: $name").invalidNel
      case value => value.validNel
    }
  }


  def children(ns: NodeSeq, name: String) : ValidationRes[Option[NodeSeq]] =
    mandatoryChildren(ns, name).toOption.validNel

  def mandatoryChildren(ns: NodeSeq, name: String) : ValidationRes[NodeSeq] = {
    ns \\ name match {
      case value if value.isEmpty => new RuntimeException(s"Missing nested node: $name").invalidNel
      case value => value.validNel
    }
  }


  def attr(ns: NodeSeq, name: String): ValidationRes[Option[String]] =
    mandatoryAttr(ns, name).toOption.validNel

  def mandatoryAttr(ns: NodeSeq, name: String): ValidationRes[String] = {
    ns \@ name match {
      case value if value.isEmpty => new RuntimeException(s"Missing attribute: $name").invalidNel
      case value => value.validNel
    }
  }


  def content(ns: NodeSeq): ValidationRes[String] = {
    ns.text match {
      case value if value.isEmpty => new RuntimeException("Missing content").invalidNel
      case value => value.validNel
    }
  }


  object implicits extends XmlTraverseSyntax
}
