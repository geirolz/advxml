package com.dg.advxml.funcs

import com.dg.advxml.Action

import scala.xml._

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
object Actions extends Actions{

  /**
    *
    * @param f
    * @return
    */
  def node(f: Node => NodeSeq): Action = nodeSeq(seq => seq.flatMap(f))

  /**
    *
    * @param f
    * @return
    */
  def nodeSeq(f: NodeSeq => NodeSeq): Action = f
}

trait Actions {
  /**
    *
    * @param ns
    * @return
    */
  def append(ns: NodeSeq) : Action = Actions.node {
    case elem: Elem => elem.copy(child = elem.child ++ ns)
    case g: Group => g.copy(nodes = g.nodes ++ ns)
    case other => other
  }

  /**
    *
    * @param ns
    * @return
    */
  def replace(ns: NodeSeq) : Action = Actions.nodeSeq(_ => ns)

  /**
    *
    * @return
    */
  def remove: Action = Actions.nodeSeq(_ => Seq.empty)

  /**
    *
    * @param key
    * @param value
    * @return
    */
  def setAttr(key: String, value: String): Action = Actions.node {
    case elem: Elem =>
      elem.copy(attributes = new UnprefixedAttribute(key, Text(value), elem.attributes))
    case other => other
  }
}
