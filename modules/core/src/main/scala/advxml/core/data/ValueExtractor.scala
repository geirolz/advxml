package advxml.core.data

trait ValueExtractor[F[_], V <: Value] {
  def extract(value: V): F[String]
}
object ValueExtractor {
  def apply[F[_], V <: Value](implicit ve: ValueExtractor[F, V]): ValueExtractor[F, V] = ve
}
