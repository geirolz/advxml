package com.dg.advxml.transform.funcs

import scala.util.Try
import scala.xml.NodeSeq

trait XmlZoom extends (NodeSeq => NodeSeq)

object Zooms extends Zooms

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * [[XmlZoom]]s are used to navigate inside a [[scala.xml.NodeSeq]] object.
  *
  * @note Do not apply transformation inside a [[XmlZoom]] function.
  * @example
  * When a function f must be applied over a specific node inside the xml document
  * you can use [[XmlZoom]] in order to zooming/focusing on that node.
  * {{{
  *   val xml: NodeSeq =
  *   <Root>
  *     <Children>
  *       <Child Index='1' />
  *       <Child Index='2' />
  *     </Children>
  *   </Root>
  *
  *   val zoom: Zoom = root => root \ "Children" \ "Child" find c => c \@ "Index" == 1
  *
  *   val result: NodeSeq = zoom(xml) //<Child Index='1' />
  * }}}
  * This trait provides all default zooms.
  * You can define your own zoom extending this [[Zooms]] trait adding your zooms functions(also composing existing [[XmlZoom]]).
  * Anyway [[XmlZoom]] is a type alias, precisely for not limit the usability.[
  *
  * @since 0.0.1
  * @author geirolad
  */
private [advxml] trait Zooms {

  /**
    * Select the first child of a [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the first element of a [[scala.xml.NodeSeq]] if collection
    *         contains at least one element, otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val firstChild: XmlZoom = childN(0)

  /**
    * Select the last child of a [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the last element of a [[scala.xml.NodeSeq]] if collection
    *         contains at least one element, otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val lastChild: XmlZoom = ns => childN(ns.length - 1)(ns)

  /**
    * Select the child at specified index in [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the element in specified position in [[scala.xml.NodeSeq]]
    *         if collection size is equals or minor of specified index,
    *         otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val childN: Int => XmlZoom = index => ns => Try(ns(index)).toOption.getOrElse(Seq.empty)
}



