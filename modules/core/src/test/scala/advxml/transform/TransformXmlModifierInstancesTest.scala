package advxml.transform

import advxml.transform.XmlZoom.root
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

import scala.util.Try
import scala.xml.NodeSeq

class TransformXmlModifierInstancesTest extends AnyFunSuite {

  import cats.implicits.*
  import advxml.implicits.*

  test("Combine AbstractRule with Semigroup instance") {

    val ns: NodeSeq         = <Root></Root>
    val rule1: AbstractRule = root ==> XmlModifier.Append(<Node1/>)
    val rule2: AbstractRule = root ==> XmlModifier.Append(<Node2/>)
    val rule3: AbstractRule = rule1 |+| rule2

    ns.transform[Try](rule3).get shouldBe <Root><Node1/><Node2/></Root>
  }
}
