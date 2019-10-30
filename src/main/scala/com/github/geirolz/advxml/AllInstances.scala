package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.XmlTextSerializerInstances
import com.github.geirolz.advxml.error.ValidationInstance
import com.github.geirolz.advxml.normalize.XmlNormalizerInstances
import com.github.geirolz.advxml.transform.XmlTransformerInstances

private[advxml] trait AllInstances
    extends XmlTransformerInstances
    with XmlTextSerializerInstances
    with XmlNormalizerInstances
    with ValidationInstance
