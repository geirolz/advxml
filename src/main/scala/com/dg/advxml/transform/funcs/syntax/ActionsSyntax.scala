package com.dg.advxml.transform.funcs.syntax

import com.dg.advxml.transform.funcs.XmlAction

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
private [funcs] trait ActionsSyntax {

  implicit class XmlActionOps(a: XmlAction) {
    def ++(that: XmlAction) : XmlAction = a.andThen(that)
  }
}
