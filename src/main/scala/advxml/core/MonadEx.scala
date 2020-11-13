package advxml.core

object MonadEx {
  def apply[F[_]: MonadEx]: MonadEx[F] = implicitly[MonadEx[F]]
}

object MonadNelEx {
  def apply[F[_]: MonadNelEx]: MonadNelEx[F] = implicitly[MonadNelEx[F]]
}
