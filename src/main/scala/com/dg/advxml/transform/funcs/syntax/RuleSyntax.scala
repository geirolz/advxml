package com.dg.advxml.transform.funcs.syntax

import com.dg.advxml.transform.funcs.{XmlAction, XmlZoom}
import com.dg.advxml.transform.{PartialXmlRule, XmlRule}

private [funcs] trait RuleSyntax {

  def current(action: XmlAction) : XmlRule = $(identity(_)) withAction action

  def $(zoom: XmlZoom): PartialXmlRule = XmlRule(zoom)

  implicit class RuleOps[T <: PartialXmlRule](r: T) {
    def ==>(action: XmlAction): XmlRule = r.withAction(action)
  }
}