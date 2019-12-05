package advxml.syntax

private[syntax] trait PredicateSyntax {

  implicit class PredicateOps[T](p: T => Boolean) {

    def &&(that: T => Boolean): T => Boolean = p.and(that)

    def and(that: T => Boolean): T => Boolean = t => p(t) && that(t)

    def ||(that: T => Boolean): T => Boolean = p.or(that)

    def or(that: T => Boolean): T => Boolean = t => p(t) || that(t)
  }
}
