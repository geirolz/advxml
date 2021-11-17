package advxml.syntax

import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}

class NestedMapSyntaxTest extends AnyFunSuite {

  import cats.instances.option.*
  import cats.instances.try_.*
  import advxml.implicits.*

  test("Test nestedMap | Try[Option[String]]") {
    val strValue: Try[Option[String]] = Success(Some("1"))
    val intValue: Try[Option[Int]]    = strValue.nestedMap(_.toInt)
    assert(intValue == Success(Some(1)))
  }

  test("Test nestedFlatMap | Try[Try[String]] | Success(Success)") {
    val strValue: Try[Try[String]] = Success(Success("1"))
    val intValue: Try[Try[Int]]    = strValue.nestedFlatMap(v => Try(v.toInt))
    assert(intValue == Success(Success(1)))
  }
}
