package com.dg

import scala.xml.NodeSeq

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
package object advxml {

  /**
    *
    */
  type Action = NodeSeq => NodeSeq

  /**
    *
    */
  type Predicate = NodeSeq => Boolean

  /**
    *
    */
  type Zoom = NodeSeq => NodeSeq
}
