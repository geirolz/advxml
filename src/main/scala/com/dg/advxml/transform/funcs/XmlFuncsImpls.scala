package com.dg.advxml.transform.funcs

import com.dg.advxml.transform.funcs.impls.{Actions, Filters, Zooms}

/**
  * Adxml
  * Created by geirolad on 18/06/2019.
  *
  * @author geirolad
  */
trait XmlFuncsImpls
  extends Actions
    with Zooms
    with Filters
