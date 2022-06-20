package advxml.xpath.utils.predicate

import cats.Monoid

object or {
  implicit def logicalOrMonoid[T]: Monoid[T => Boolean] =
    new Monoid[T => Boolean] {
      override def empty: T => Boolean = _ => false

      override def combine(x: T => Boolean, y: T => Boolean): T => Boolean =
        t => x(t) || y(t)
    }
}
