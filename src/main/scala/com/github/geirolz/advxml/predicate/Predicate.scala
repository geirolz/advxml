package com.github.geirolz.advxml.predicate

/**
  * Advxml
  * Created by geirolad on 26/06/2019.
  *
  * @author geirolad
  */
object Predicate {
  def and[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) && p2(t)
  def or[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) || p2(t)
}

private[advxml] trait PredicateSyntax {

  implicit class PredicateOps[T](p: T => Boolean) {

    def &&(that: T => Boolean): T => Boolean = p.and(that)

    def and(that: T => Boolean): T => Boolean = t => p(t) && that(t)

    def ||(that: T => Boolean): T => Boolean = p.or(that)

    def or(that: T => Boolean): T => Boolean = t => p(t) || that(t)
  }
}
