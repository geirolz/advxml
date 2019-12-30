package advxml.syntax

import org.scalatest.FeatureSpec

import scala.util.Try
import scala.xml.Elem

class XmlTransformerSyntaxTest extends FeatureSpec {

  import advxml.instances.convert._
  import advxml.instances.transform._
  import advxml.syntax.transform._
  import cats.instances.try_._

  feature("Xml manipulation: Filters") {
    scenario("Filter By Attribute") {
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
  }

  feature("Xml manipulation: Nodes") {
    scenario("PrependNode") {
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

    scenario("AppendNode") {
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

    scenario("ReplaceNode") {
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

    scenario("RemoveNode") {
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

    scenario("RemoveNode root") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(root ==> Remove)

      assert(result.get.isEmpty)
    }

    scenario("AppendNode to Root") {
      val elem: Elem = <OrderLines />
      val result = elem
        .transform[Try](
          root ==> Append(<OrderLine PrimeLineNo="1"/>)
        )
        .get

      assert((result \ "OrderLine").length == 1)
      assert(result \ "OrderLine" \@ "PrimeLineNo" == "1")
    }
  }

  feature("Xml manipulation: Attributes") {

    scenario("SetAttribute") {
      val elem: Elem = <Order><OrderLines /></Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> SetAttrs("A1" := "1", "A2" := "2", "A3" := "3")
      )

      assert(result.get \ "OrderLines" \@ "A1" == "1")
      assert(result.get \ "OrderLines" \@ "A2" == "2")
      assert(result.get \ "OrderLines" \@ "A3" == "3")
    }

    scenario("SetAttribute to root") {
      val elem: Elem = <Order />

      val result = elem.transform(
        root ==> SetAttrs("A1" := "1", "A2" := "2", "A3" := "3")
      )

      assert(result.get \@ "A1" == "1")
      assert(result.get \@ "A2" == "2")
      assert(result.get \@ "A3" == "3")
    }

    scenario("ReplaceAttribute") {
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

    scenario("RemoveAttribute") {
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
}
