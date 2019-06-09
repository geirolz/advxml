package com.dg.advxml.funcs

import com.dg.advxml.Zoom

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * A [[Zoom]] is used to navigate inside [[scala.xml.NodeSeq]], when a function {@}
  * This trait provides all default zooms.
  * You can define your own zoom extending this [[Zooms]] trait adding your zooms functions(also composing existing [[Zoom]]).
  * Anyway [[Zoom]] is a type alias, precisely for not limit the usability.[[Zoom]]s are just
  * functions from domain [[scala.xml.NodeSeq]] to co-domain [[scala.xml.NodeSeq]]
  * @author geirolad
  */
trait Zooms {

  /**
    * Select the first child of a [[scala.xml.NodeSeq]] collection
    *
    * @return When applied return the first element of a [[scala.xml.NodeSeq]] if collection
    *         contains at least one element, otherwise return an empty [[scala.xml.NodeSeq]]
    */
  lazy val firstChild: Zoom = _.headOption.getOrElse(Seq.empty)
}

/**
  * @inheritdoc
  */
object Zooms extends Zooms



