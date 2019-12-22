package advxml.syntax

import cats.{Applicative, Monad}
import cats.implicits._

trait NestedMapSyntax {
  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}
