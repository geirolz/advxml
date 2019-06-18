package com.dg.advxml.transform.funcs

import scala.xml.{Node, NodeSeq}

trait XmlPredicate extends (NodeSeq => Boolean){

  def &&(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) && that(xml)

  def ||(that: XmlPredicate) : XmlPredicate =
    xml => this(xml) || that(xml)
}

object Filters extends Filters

private [transform] trait Filters {

  import com.dg.advxml.syntax.XmlSyntax._

  val always: XmlPredicate = _ => true

  def text(text: String) : XmlPredicate = {
    case n: Node => n.text == text
    case _ => false
  }

  def label(name: String): XmlPredicate = {
    case n: Node => n.label == name
    case _ => false
  }

  def attrs(value: (String, String), values: (String, String)*): XmlPredicate = {

    def attr(key: String, value: String): XmlPredicate =
      _ \@ key == value

    (Seq(value) ++ values)
      .map(t => attr(t._1, t._2))
      .reduce(_ && _)
  }

//TODO CHECK THIS PREDICATE
  def hasImmediateChild(name: String, predicate: XmlPredicate = always) : XmlPredicate =
    xml => (xml \? name).fold(false)(_.exists(predicate))

  def count(length: Int) : XmlPredicate = _.length == length

  def equalsTo(ns: NodeSeq): XmlPredicate = that => (ns, that) match {
    case (e1: Node, e2: Node) => e1 strict_== e2
    case (ns1: NodeSeq, ns2: NodeSeq) => ns1 strict_== ns2
    case _ => false
  }
}