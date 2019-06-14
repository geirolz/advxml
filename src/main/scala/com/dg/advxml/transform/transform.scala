package com.dg.advxml

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 14/06/2019.
  *
  * @author geirolad
  */
package object transform {

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
