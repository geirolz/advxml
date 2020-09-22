package advxml.core.utils

/**
  * This type describes a traversable behavior of a [[Traversable]] of type [[C]]
  * that contains items of [[I]] safety in [[F]].
  *
  * @tparam I Item type.
  * @tparam C [[Traversable]] of type [[I]].
  * @tparam F Higher-kind type for safe return values.
  */
//TODO: Common
trait TraverserK[I, C <: Traversable[I], F[_]] extends Any {

  /**
    * Find the item at the specified index.
    * @param c [[Traversable]] instance.
    * @param idx Item index(starts from zero).
    * @return
    */
  def atIndexF(c: C, idx: Int): F[I]

  /**
    * Select the head item of the [[Traversable]].
    * @param c [[Traversable]] instance.
    * @return the first element of the [[Traversable]].
    */
  def headF(c: C): F[I]

  /**
    * Select the last item of the [[Traversable]]
    * @param c [[Traversable]] instance.
    * @return the first element of the [[Traversable]].
    */
  def lastF(c: C): F[I]

  /**
    * Select the tail of the [[Traversable]]
    * @param c [[Traversable]] instance.
    * @return the [[Traversable]] tail.
    */
  def tailF(c: C): F[C]

  /**
    * Find the first item satisfy the predicate.
    * @param c [[Traversable]] instance.
    * @param p Predicate used to match item.
    * @return change by the implementation of this type.
    */
  def findF(c: C, p: I => Boolean): F[I]

  /**
    * Filter the traversable and keep only the items where the predicate {{{ p(item) = true}}}.
    * @param c Traversable instance.
    * @param p Predicate to filter items.
    * @return change by the implementation of this type.
    */
  def filterF(c: C, p: I => Boolean): F[C]
}
