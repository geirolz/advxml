package advxml.core

object Predicate {

  /** Combine two predicates(`T => Boolean`) with `And` operator.
    *
    * @param p1 First predicate
    * @param p2 Second predicate
    * @tparam T Subject type of predicates
    * @return Result of combination of `p1` and `p2` using `And` operator.
    */
  def and[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) && p2(t)

  /** Combine two predicates(`T => Boolean`) with `Or` operator.
    *
    * @param p1 First predicate
    * @param p2 Second predicate
    * @tparam T Subject type of predicates
    * @return Result of combination of `p1` and `p2` using `Or` operator.
    */
  def or[T](p1: T => Boolean, p2: T => Boolean): T => Boolean = t => p1(t) || p2(t)
}
