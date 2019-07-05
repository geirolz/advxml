package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.{ValidationInstances, XmlTextSerializerInstances}
import com.github.geirolz.advxml.transform.XmlTransformerInstances

object instances extends XmlTransformerInstances
    with XmlTextSerializerInstances
    with ValidationInstances
