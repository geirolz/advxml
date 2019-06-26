package com.dg.advxml.transform

import org.scalatest.FeatureSpec

import scala.xml.Elem

class XmlTransformerTest extends FeatureSpec  {

  import com.dg.advxml.AdvXml._

  feature("Xml manipulation: Filters") {
    scenario("Filter By Attribute") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
          <OrderLine PrimeLineNo="3" />
        </OrderLines>
      </Order>

      val result = elem \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")

      assert(result \@ "PrimeLineNo" == "1")
    }
  }

  feature("Xml manipulation: Nodes") {

    scenario("AppendNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val result = elem.transform(
          $(_ \ "OrderLines")
            ==> Append(<OrderLine PrimeLineNo="2" />)
            ==> Append(<OrderLine PrimeLineNo="3" />)
            ==> Append(<OrderLine PrimeLineNo="4" />)
      )

      assert(result \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "1"))
      assert(result \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "2"))
      assert(result \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "3"))
      assert(result \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "4"))
    }

    scenario("ReplaceNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1"))
          ==> Replace(<OrderLine PrimeLineNo="4" />)
      )

      assert((result \ "OrderLines" \ "OrderLine"
        filter attrs("PrimeLineNo" -> "1")).length == 0)
      assert(result \ "OrderLines" \ "OrderLine"
        exists attrs("PrimeLineNo" -> "4"))
    }

    scenario("RemoveNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")) ==> Remove
      )

      assert((result \ "OrderLines" \ "OrderLine"
        filter attrs("PrimeLineNo" -> "1")).length == 0)
    }

    scenario("RemoveNode root") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(Remove)

      assert(result.isEmpty)
    }

    scenario("AppendNode to Root"){
      val elem: Elem = <OrderLines />
      val result = elem.transform(
        Append(<OrderLine PrimeLineNo="1" />)
      )

      assert((result \ "OrderLine").length == 1)
      assert(result \ "OrderLine" \@ "PrimeLineNo" == "1")
    }
  }

  feature("Xml manipulation: Attributes") {

    scenario("SetAttribute") {
      val elem: Elem = <Order><OrderLines /></Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> SetAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result \ "OrderLines" \@ "A1" == "1")
      assert(result \ "OrderLines" \@ "A2" == "2")
      assert(result \ "OrderLines" \@ "A3" == "3")
    }

    scenario("SetAttribute to root") {
      val elem: Elem = <Order />

      val result = elem.transform(
        SetAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result \@ "A1" == "1")
      assert(result \@ "A2" == "2")
      assert(result \@ "A3" == "3")
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
        $(_ \ "OrderLines") ==> SetAttrs("T1" -> "EDITED")
      )

      assert(result \ "OrderLines" \@ "T1" == "EDITED")
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
        $(_ \ "OrderLines") ==> RemoveAttrs("T1")
      )

      assert(result \ "OrderLines" \@ "T1" == "")
    }
  }
}
