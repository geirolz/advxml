package advxml

import cats.{~>, Eval, MonadError}

import scala.annotation.implicitNotFound

package object core {

  type MonadEx[F[_]] = MonadError[F, Throwable]

  @implicitNotFound(
    "Cannot find an implicit value for OptErrorHandler of type ${F}. Please try to import advxml._"
  )
  type OptErrorHandler[F[_]] = Eval[Throwable] => Option ~> F

  //TypeInequalities
  type =:!=[A, B] = TypeInequalities.=:!=[A, B]
}
