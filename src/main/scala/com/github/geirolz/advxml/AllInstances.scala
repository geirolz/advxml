package com.github.geirolz.advxml

import com.github.geirolz.advxml.convert.ConvertersInstances
import com.github.geirolz.advxml.validate.ValidationInstance
import com.github.geirolz.advxml.transform.XmlTransformerInstances

private[advxml] trait AllInstances extends XmlTransformerInstances with ConvertersInstances with ValidationInstance
