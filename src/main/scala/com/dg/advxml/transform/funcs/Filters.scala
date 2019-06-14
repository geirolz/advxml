package com.dg.advxml.transform.funcs

import com.dg.advxml.transform.Predicate

import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
trait Filters {

  def text(text: String) : Predicate = {
    case n: Node => n.text == text
    case _ => false
  }

  def label(name: String): Predicate = {
    case n: Node => n.label == name
    case _ => false
  }

  def attr(key: String, value: String): Predicate =
    _ \@ key == value

  def equalsTo(ns: NodeSeq): Predicate = that => (ns, that) match {
    case (e1: Node, e2: Node) => e1 strict_== e2
    case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
    case _ => false
  }
}

/**
  * @inheritdoc
  */
object Filters  extends Filters