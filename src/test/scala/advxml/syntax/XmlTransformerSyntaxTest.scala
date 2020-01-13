package advxml.syntax

import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{Elem, NodeSeq}
import scala.xml.transform.RuleTransformer

class XmlTransformerSyntaxTest extends AnyFunSuite {

  import advxml.instances.convert._
  import advxml.instances.transform._
  import advxml.syntax.normalize._
  import advxml.syntax.transform._
  import cats.instances.try_._

  test("Transform syntax with custom BasicTransformer") {
    val elem: Elem = <Order>
      <OrderLines>
        <OrderLine PrimeLineNo="1" />
      </OrderLines>
    </Order>

    val result = elem
      .transform(new RuleTransformer(_: _*))(
        $(_ \ "OrderLines") ==> Append(<OrderLine PrimeLineNo="2"/>)
      )

    val orderLinesResult = (result.get \ "OrderLines" \ "OrderLine").toList
    assert(orderLinesResult.size == 2)
    assert(orderLinesResult.head \@ "PrimeLineNo" == "1")
    assert(orderLinesResult(1) \@ "PrimeLineNo" == "2")
  }

  test("Filter By Attribute") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
          <OrderLine PrimeLineNo="3" />
        </OrderLines>
      </Order>

    val result = elem \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> (_ == "1"))

    assert(result \@ "PrimeLineNo" == "1")
  }

  test("PrependNode") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="4" />
        </OrderLines>
      </Order>

    val result = elem.transform(
      $(_ \ "OrderLines")
      ==> Prepend(<OrderLine PrimeLineNo="3"/>)
      ==> Prepend(<OrderLine PrimeLineNo="2"/>)
      ==> Prepend(<OrderLine PrimeLineNo="1"/>)
    )

    val orderLinesResult = (result.get \ "OrderLines" \ "OrderLine").toList
    assert(orderLinesResult.size == 4)
    assert(orderLinesResult.head \@ "PrimeLineNo" == "1")
    assert(orderLinesResult(1) \@ "PrimeLineNo" == "2")
    assert(orderLinesResult(2) \@ "PrimeLineNo" == "3")
    assert(orderLinesResult(3) \@ "PrimeLineNo" == "4")
  }

  test("AppendNode") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

    val result = elem
      .transform(
        $(_ \ "OrderLines")
        ==> Append(<OrderLine PrimeLineNo="2"/>)
        ==> Append(<OrderLine PrimeLineNo="3"/>)
        ==> Append(<OrderLine PrimeLineNo="4"/>)
      )

    val orderLinesResult = (result.get \ "OrderLines" \ "OrderLine").toList
    assert(orderLinesResult.size == 4)
    assert(orderLinesResult.head \@ "PrimeLineNo" == "1")
    assert(orderLinesResult(1) \@ "PrimeLineNo" == "2")
    assert(orderLinesResult(2) \@ "PrimeLineNo" == "3")
    assert(orderLinesResult(3) \@ "PrimeLineNo" == "4")
  }

  test("ReplaceNode") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

    val result = elem.transform(
      $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> (_ == "1")))
      ==> Replace(_ => <OrderLine PrimeLineNo="4"/>)
    )

    assert(
      (result.get \ "OrderLines" \ "OrderLine"
      filter attrs("PrimeLineNo" -> (_ == "1"))).length == 0
    )
    assert(
      result.get \ "OrderLines" \ "OrderLine"
      exists attrs("PrimeLineNo" -> (_ == "4"))
    )
  }

  test("Replace With same node") {
    val xml = <A><B>1</B></A>
    val result: Try[NodeSeq] = xml.transform(
      $(_ \ "B") ==> Replace(_ => <B>1</B>)
    )

    assert(result.get === <A><B>1</B></A>)
  }

  test("RemoveNode") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

    val result = elem.transform(
      $(
        _ \ "OrderLines" \ "OrderLine" filter
        attrs("PrimeLineNo" -> (_ == "1"))
      ) ==> Remove
    )

    assert(
      (result.get \ "OrderLines" \ "OrderLine"
      filter attrs("PrimeLineNo" -> (_ == "1"))).length == 0
    )
  }

  test("RemoveNode root") {
    val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

    val result = elem.transform(root ==> Remove)

    assert(result.get.isEmpty)
  }

  test("AppendNode to Root") {
    val elem: Elem = <OrderLines />
    val result = elem
      .transform[Try](
        root ==> Append(<OrderLine PrimeLineNo="1"/>)
      )
      .get

    assert((result \ "OrderLine").length == 1)
    assert(result \ "OrderLine" \@ "PrimeLineNo" == "1")
  }

  test("SetAttribute") {
    val elem: Elem = <Order><OrderLines /></Order>

    val result = elem.transform(
      $(_ \ "OrderLines") ==> SetAttrs("A1" := "1", "A2" := "2", "A3" := "3")
    )

    assert(result.get \ "OrderLines" \@ "A1" == "1")
    assert(result.get \ "OrderLines" \@ "A2" == "2")
    assert(result.get \ "OrderLines" \@ "A3" == "3")
  }

  test("SetAttribute to root") {
    val elem: Elem = <Order />

    val result = elem.transform(
      root ==> SetAttrs("A1" := "1", "A2" := "2", "A3" := "3")
    )

    assert(result.get \@ "A1" == "1")
    assert(result.get \@ "A2" == "2")
    assert(result.get \@ "A3" == "3")
  }

  test("ReplaceAttribute") {
    val elem: Elem = <Order>
        <OrderLines T1="1">
          <OrderLine PrimeLineNo="1"></OrderLine>
          <OrderLine PrimeLineNo="2"></OrderLine>
          <OrderLine PrimeLineNo="3"></OrderLine>
        </OrderLines>
      </Order>

    val result = elem.transform(
      $(_ \ "OrderLines") ==> SetAttrs("T1" := "EDITED")
    )

    assert(result.get \ "OrderLines" \@ "T1" == "EDITED")
  }

  test("RemoveAttribute") {
    val elem: Elem = <Order>
        <OrderLines T1="1">
          <OrderLine PrimeLineNo="1"></OrderLine>
          <OrderLine PrimeLineNo="2"></OrderLine>
          <OrderLine PrimeLineNo="3"></OrderLine>
        </OrderLines>
      </Order>

    val result = elem.transform(
      $(_ \ "OrderLines") ==> RemoveAttrs(_.key == "T1")
    )

    assert(result.get \ "OrderLines" \@ "T1" == "")
  }
}
