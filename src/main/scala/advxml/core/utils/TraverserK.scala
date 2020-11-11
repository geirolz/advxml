package advxml.core.utils

/** This type describes a traversable behavior of a [[Iterable]] of type [[CI]]
  * that contains items of [[I]] safety in [[F]].
  *
  * @tparam I Item type.
  * @tparam C [[Iterable]] of type [[I]].
  * @tparam F Higher-kind type for safe return values.
  */
//TODO: Common
trait TraverserK[I, C <: Iterable[I], F[_]] extends Any {

  /** Find the item at the specified index.
    * @param c [[Iterable]] instance.
    * @param idx Item index(starts from zero).
    * @return
    */
  def atIndex(c: C, idx: Int): F[I]

  /** Select the head item of the [[Iterable]].
    * @param c [[Iterable]] instance.
    * @return the first element of the [[Iterable]].
    */
  def head(c: C): F[I]

  /** Select the last item of the [[Iterable]]
    * @param c [[Iterable]] instance.
    * @return the first element of the [[Iterable]].
    */
  def last(c: C): F[I]

  /** Select the tail of the [[Iterable]]
    * @param c [[Iterable]] instance.
    * @return the [[Iterable]] tail.
    */
  def tail(c: C): F[C]

  /** Find the first item satisfy the predicate.
    * @param c [[Iterable]] instance.
    * @param p Predicate used to match item.
    * @return change by the implementation of this type.
    */
  def find(c: C, p: I => Boolean): F[I]

  /** Filter the traversable and keep only the items where the predicate {{{ p(item) = true}}}.
    * @param c Traversable instance.
    * @param p Predicate to filter items.
    * @return change by the implementation of this type.
    */
  def filter(c: C, p: I => Boolean): F[C]
}
