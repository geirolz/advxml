package advxml.testUtils

import scala.util.Try

trait Fallible[F[_]] {
  def extract[T](fa: F[T]): T
  def isFailure[T](fa: F[T]): Boolean
}

object Fallible {

  def apply[F[_]: Fallible]: Fallible[F] = implicitly[Fallible[F]]

  implicit class FallibleSyntaxOps[F[_]: Fallible, A](fa: F[A]) {
    def extract: A = Fallible[F].extract(fa)
    def isFailure: Boolean = Fallible[F].isFailure(fa)
  }

  implicit val advxmlTryFallibleInstance: Fallible[Try] = new Fallible[Try] {
    override def isFailure[T](fa: Try[T]): Boolean = fa.isFailure
    override def extract[A](fa: Try[A]): A = fa.get
  }

  implicit val advxmlOptionFallibleInstance: Fallible[Option] = new Fallible[Option] {
    override def isFailure[T](fa: Option[T]): Boolean = fa.isEmpty
    override def extract[T](fa: Option[T]): T = fa.get
  }
}
