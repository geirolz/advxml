package advxml.core

object MonadEx {
  def apply[F[_]: MonadEx]: MonadEx[F] = implicitly[MonadEx[F]]
}
