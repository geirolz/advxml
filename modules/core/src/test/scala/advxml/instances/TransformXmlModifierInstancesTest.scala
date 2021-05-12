package advxml.instances

import advxml.core.transform.AbstractRule
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.util.Try
import scala.xml.NodeSeq

class TransformXmlModifierInstancesTest extends AnyFunSuite {

  import advxml.implicits._
  import cats.implicits._

  test("Combine AbstractRule with Semigroup instance") {

    val ns: NodeSeq = <Root></Root>
    val rule1: AbstractRule = root ==> Append(<Node1/>)
    val rule2: AbstractRule = root ==> Append(<Node2/>)
    val rule3: AbstractRule = rule1 |+| rule2

    ns.transform[Try](rule3).get shouldBe <Root><Node1/><Node2/></Root>
  }
}
