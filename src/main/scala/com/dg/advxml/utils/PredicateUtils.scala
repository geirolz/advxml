package com.dg.advxml.utils

/**
  * Adxml
  * Created by geirolad on 26/06/2019.
  *
  * @author geirolad
  */
private [advxml] object PredicateUtils {
  def and[T](p1: T => Boolean, p2: T => Boolean) : T => Boolean = t => p1(t) && p2(t)
  def or[T](p1: T => Boolean, p2: T => Boolean) : T => Boolean = t => p1(t) || p2(t)
}
