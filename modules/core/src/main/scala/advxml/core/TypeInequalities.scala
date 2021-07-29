package advxml.core

import scala.annotation.implicitNotFound

//noinspection ScalaFileName
// $COVERAGE-OFF$
@implicitNotFound(msg = "Cannot prove that ${A} =:!= ${B}.")
sealed trait =:!=[A, B]

//noinspection ScalaFileName
object =:!= {
  implicit def neq[A, B]: A =:!= B = new =:!=[A, B] {}
  implicit def neqAmbig1[A]: A =:!= A = null
  implicit def neqAmbig2[A]: A =:!= A = null
}
// $COVERAGE-ON$
