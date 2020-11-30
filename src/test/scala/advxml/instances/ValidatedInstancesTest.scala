package advxml.instances

import advxml.core.data.{ThrowableNel, ValidatedEx, ValidatedNelEx}
import cats.Eq
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FunSuiteDiscipline

class ValidatedInstancesTest extends AnyFunSuite with FunSuiteDiscipline with Configuration {

  import advxml.instances.validated._
  import cats.implicits._
  import cats.laws.discipline.arbitrary._

  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual

  checkAll(
    "MonadTests[ValidatedNelEx, Throwable]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedNelEx, Throwable]
      .monad[Int, Int, Int]
  )

  checkAll(
    "MonadErrorTests[ValidatedEx, ThrowableNel]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedEx, ThrowableNel]
      .monadError[Int, Int, Int]
  )
}
