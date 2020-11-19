package advxml

import cats.{~>, Eval, MonadError}

import scala.annotation.implicitNotFound

package object core {

  // Type inequalities
  type =:!=[A, B] = TypeInequalities[A, B]
  implicit def neq[A, B]: A TypeInequalities B = new TypeInequalities[A, B] {}
  implicit def neqAmbig1[A]: A TypeInequalities A = unexpected
  implicit def neqAmbig2[A]: A TypeInequalities A = unexpected
  private def unexpected: Nothing = sys.error("Unexpected invocation A eq to B")

  type MonadEx[F[_]] = MonadError[F, Throwable]

  @implicitNotFound(
    "Cannot find an implicit value for OptErrorHandler of type ${F}. Please try to import advxml._"
  )
  type OptErrorHandler[F[_]] = Eval[Throwable] => Option ~> F
}
