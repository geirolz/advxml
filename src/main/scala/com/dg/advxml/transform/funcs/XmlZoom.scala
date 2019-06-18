package com.dg.advxml.transform.funcs

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
trait XmlZoom extends (NodeSeq => NodeSeq){
  def andThen(that: XmlZoom): XmlZoom = xml => that(this(xml))
}
