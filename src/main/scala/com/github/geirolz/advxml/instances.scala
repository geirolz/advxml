package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationInstances, XmlTextSerializerInstances}
import com.github.geirolz.advxml.transform.XmlTransformerInstances

object instances extends XmlTransformerInstances
    with XmlTextSerializerInstances
    with ValidationInstances {

  object transformer extends XmlTransformerInstances
  object validation extends ValidationInstances
  object textSerializer extends XmlTextSerializerInstances
}
