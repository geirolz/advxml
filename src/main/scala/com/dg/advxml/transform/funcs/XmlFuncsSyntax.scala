package com.dg.advxml.transform.funcs

import com.dg.advxml.transform.funcs.syntax.{ActionsSyntax, PredicateSyntax, RuleSyntax, ZoomSyntax}

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
trait XmlFuncsSyntax
  extends RuleSyntax
      with ActionsSyntax
      with ZoomSyntax
      with PredicateSyntax
