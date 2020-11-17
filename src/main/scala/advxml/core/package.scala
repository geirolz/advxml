package advxml

import advxml.core.data.ThrowableNel
import cats.{~>, MonadError}

import scala.annotation.implicitNotFound

package object core {

  // Type inequalities
  type =:!=[A, B] = TypeInequalities[A, B]

  implicit def neq[A, B]: A TypeInequalities B = new TypeInequalities[A, B] {}

  implicit def neqAmbig1[A]: A TypeInequalities A = unexpected

  implicit def neqAmbig2[A]: A TypeInequalities A = unexpected
  private def unexpected: Nothing = sys.error("Unexpected invocation A eq to B")

  type MonadEx[F[_]] = MonadError[F, Throwable]
  type MonadNelEx[F[_]] = MonadError[F, ThrowableNel]

  @implicitNotFound(
    "Cannot find an implicit value for OptErrorHandler of type ${F}. Please try to import advxml._"
  )
  type OptErrorHandler[F[_]] = Throwable => Option ~> F
}
