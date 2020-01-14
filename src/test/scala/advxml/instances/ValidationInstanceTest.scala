package advxml.instances

import advxml.core.validate.{ThrowableNel, ValidatedEx}
import cats.Eq
import org.scalatest.funsuite.AnyFunSuite
import org.typelevel.discipline.scalatest.Discipline

class ValidationInstanceTest extends AnyFunSuite with Discipline {

  import advxml.instances.validate._
  import cats.implicits._
  import cats.laws.discipline.arbitrary._

  implicit val eqThrowable: Eq[Throwable] = Eq.allEqual

  checkAll(
    "MonadTests[ValidatedEx, Throwable]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedEx, Throwable]
      .monad[Int, Int, Int] //TODO: monadError
  )

  checkAll(
    "MonadTests[ValidatedEx, ThrowableNel]",
    cats.laws.discipline
      .MonadErrorTests[ValidatedEx, ThrowableNel]
      .monadError[Int, Int, Int]
  )
}
