package advxml

import org.scalatest.flatspec.AnyFlatSpec

class TypeInequalitiesTest extends AnyFlatSpec {

  import org.scalatest.matchers.should.Matchers.*

  """
    def check[A, B](implicit ne: A =:!= B): Boolean = ne != null
    val res: Boolean = check[String, Int]
    """ should compile

  """
    def check[A, B](implicit ne: A =:!= B): Boolean = ne != null
    val res: Boolean = check[String, String]
    """ shouldNot compile
}
