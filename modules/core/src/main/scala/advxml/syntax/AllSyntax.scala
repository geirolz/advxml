package advxml.syntax

import cats.{Applicative, Monad}

private[advxml] trait AllSyntax
    extends AllCommonSyntax
    with AllTransformSyntax
    with AllDataSyntax
    with NormalizerSyntax
    with JavaScalaConvertersSyntax

private[advxml] trait AllCommonSyntax extends NestedMapSyntax

//============================== NESTED MAP ==============================
private[syntax] trait NestedMapSyntax {

  import cats.implicits._

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}
