package advxml.instances

import advxml.core.data.ValidatedNelEx
import advxml.core.MonadEx
import cats.Eq
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
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

  test("MonadError[ValidatedNelEx, Throwable].raiseError") {
    val exception = new RuntimeException("ERROR")
    assert(
      MonadEx[ValidatedNelEx].raiseError(exception) == Invalid(NonEmptyList.one(exception))
    )
  }

  test("MonadError[ValidatedNelEx, Throwable].handleWith - Valid") {
    val fa: ValidatedNelEx[Int] = Valid(1)
    assert(MonadEx[ValidatedNelEx].handleError(fa)(_ => 1) == Valid(1))
  }

  test("MonadError[ValidatedNelEx, Throwable].handleWith - Invalid") {
    val fa: ValidatedNelEx[Int] = Invalid(NonEmptyList.one(new RuntimeException("ERROR")))
    assert(MonadEx[ValidatedNelEx].handleError(fa)(_ => -1) == Valid(-1))
  }

  test("MonadError[ValidatedNelEx, Throwable].handleErrorWith - Valid") {
    val fa: ValidatedNelEx[Int] = Valid(1)
    assert(MonadEx[ValidatedNelEx].handleErrorWith(fa)(_ => Valid(-1)) == Valid(1))
  }

  test("MonadError[ValidatedNelEx, Throwable].handleErrorWith - Invalid") {
    val fa: ValidatedNelEx[Int] = Invalid(NonEmptyList.one(new RuntimeException("ERROR")))
    assert(MonadEx[ValidatedNelEx].handleErrorWith(fa)(_ => Valid(-1)) == Valid(-1))
  }
}
