package advxml.core

import org.scalatest.flatspec.AnyFlatSpec

class TypeInequalitiesTest extends AnyFlatSpec {

  import org.scalatest.matchers.should.Matchers._

  """
    |def check[A, B](implicit ne: A =:!= B): Boolean = ne != null
    |val res: Boolean = check[String, Int]
    |""".stripMargin should compile

  """
    |def check[A, B](implicit ne: A =:!= B): Boolean = ne != null
    |val res: Boolean = check[String, String]
    |""".stripMargin shouldNot compile
}
