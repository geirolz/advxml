package advxml.core

object Predicate {
  def and[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) && p2(t)
  def or[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) || p2(t)
}
