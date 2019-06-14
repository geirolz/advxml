package com.dg.advxml

import com.dg.advxml.transform.XmlTransformer

private [advxml] sealed trait AdvXml
  extends XmlTransformer

object AdvXml extends AdvXml
