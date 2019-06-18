package com.dg.advxml.transform.funcs

import scala.xml.NodeSeq

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
trait XmlAction extends (NodeSeq => NodeSeq){
	def andThen(that: NodeSeq => NodeSeq) : XmlAction = xml => that(this(xml))
}
