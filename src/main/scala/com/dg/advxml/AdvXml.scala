package com.dg.advxml

import com.dg.advxml.core.funcs.{Actions, Filters, Zooms}
import com.dg.advxml.core.{RuleSyntax, XmlTransformerImplicits}

private [advxml] sealed trait AdvXml
  extends XmlTransformerImplicits
    with RuleSyntax
    with Filters
    with Zooms
    with Actions

object AdvXml extends AdvXml
