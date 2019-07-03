package com.github.geirolz.advxml.traverse

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

/**
  * Advxml
  * Created by geirolad on 28/06/2019.
  *
  * @author geirolad
  */
object XmlTraverser {

  def immediateChildren(ns: NodeSeq, name: String) : Option[NodeSeq] =
    mandatoryImmediateChildren(ns, name).toOption

  def mandatoryImmediateChildren(ns: NodeSeq, name: String) : Try[NodeSeq] = {
    ns \ name match {
      case value if value.isEmpty => Failure(new RuntimeException(s"Missing node: $name"))
      case value => Success(value)
    }
  }


  def children(ns: NodeSeq, name: String) : Option[NodeSeq] =
    mandatoryChildren(ns, name).toOption

  def mandatoryChildren(ns: NodeSeq, name: String) : Try[NodeSeq] = {
    ns \\ name match {
      case value if value.isEmpty => Failure(new RuntimeException(s"Missing nested node: $name"))
      case value => Success(value)
    }
  }


  def attr(ns: NodeSeq, name: String): Option[String] = mandatoryAttr(ns, name).toOption

  def mandatoryAttr(ns: NodeSeq, name: String): Try[String] = {
    ns \@ name match {
      case value if value.isEmpty => Failure(new RuntimeException(s"Missing attribute: $name"))
      case value => Success(value)
    }
  }


  def content(ns: NodeSeq): Try[String] = {
    ns.text match {
      case value if value.isEmpty => Failure(new RuntimeException("Missing content"))
      case value => Success(value)
    }
  }


  object ops extends XmlTraverseSyntax
}
