package com.dg.advxml.transform.funcs

import com.dg.advxml.transform.core.Zoom

import scala.util.Try

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * [[Zoom]]s are used to navigate inside a [[scala.xml.NodeSeq]] object.
  *
  * @note Do not apply transformation inside a [[Zoom]] function.
  * @example
  * When a function f must be applied over a specific node inside the xml document
  * you can use [[Zoom]] in order to zooming/focusing on that node.
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
  * You can define your own zoom extending this [[Zooms]] trait adding your zooms functions(also composing existing [[Zoom]]).
  * Anyway [[Zoom]] is a type alias, precisely for not limit the usability.[[Zoom]]s are just
  * functions from domain [[scala.xml.NodeSeq]] to co-domain [[scala.xml.NodeSeq]] so you can create your
  * own [[Zoom]] where ever you want.
  *
  * @since 0.0.1
  * @author geirolad
  */
trait Zooms {

  lazy val current: Zoom = r => r

  /**
    * Select the first child of a [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the first element of a [[scala.xml.NodeSeq]] if collection
    *         contains at least one element, otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val firstChild: Zoom = childN(0)

  /**
    * Select the last child of a [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the last element of a [[scala.xml.NodeSeq]] if collection
    *         contains at least one element, otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val lastChild: Zoom = ns => childN(ns.length - 1)(ns)

  /**
    * Select the child at specified index in [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the element in specified position in [[scala.xml.NodeSeq]]
    *         if collection size is equals or minor of specified index,
    *         otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val childN: Int => Zoom = index => ns => Try(ns(index)).toOption.getOrElse(Seq.empty)
}

/**
  * @inheritdoc
  */
object Zooms extends Zooms



