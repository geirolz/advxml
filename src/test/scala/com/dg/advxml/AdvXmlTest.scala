package com.dg.advxml

import org.scalatest.FeatureSpec

import scala.xml.Elem
import scala.xml.Utility.trim

class AdvXmlTest extends FeatureSpec  {

  import com.dg.advxml.AdvXml._
  import org.scalatest.StreamlinedXmlEquality._

  feature("Xml manipulation: Filters") {
    scenario("Filter By Attribute") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
          <OrderLine PrimeLineNo="3" />
        </OrderLines>
      </Order>

      val expected: Elem = <OrderLine PrimeLineNo="1" />
      val result = elem \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")

      assert(result === trim(expected))
    }
  }

  feature("Xml manipulation: Nodes") {

    scenario("AppendNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val expected: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>


      val result = elem.transform(
          $(_ \ "OrderLines")
          ==> append(<OrderLine PrimeLineNo="2" />)
          ==> append(<OrderLine PrimeLineNo="2" />)
          ==> append(<OrderLine PrimeLineNo="2" />)
          ==> append(<OrderLine PrimeLineNo="2" />)
      )

      assert(result === trim(expected))
    }

    scenario("ReplaceNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>

      val expected: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="4" />
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1"))
          ==> replace(<OrderLine PrimeLineNo="4" />)
      )

      assert(result === trim(expected))
    }

    scenario("RemoveNode") {
      val elem: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val expected: Elem = <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="2" />
        </OrderLines>
      </Order>

      val result = elem.transform(
        $(_ \ "OrderLines" \ "OrderLine" filter attrs("PrimeLineNo" -> "1")) ==> remove
      )

      assert(result === trim(expected))
    }

    scenario("AppendNode to Root"){
      val elem: Elem = <OrderLines />

      val expected: Elem =
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>


      val result = elem.transform(append(<OrderLine PrimeLineNo="1" />))

      assert(result === trim(expected))
    }
  }

  feature("Xml manipulation: Attributes") {

    scenario("SetAttribute") {
      val elem: Elem = <Order><OrderLines /></Order>
      val expected: Elem = <Order><OrderLines A1="1" A2="2" A3="3" /></Order>

      val result = elem.transform(
        $(_ \ "OrderLines") ==> setAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result === trim(expected))
    }

    scenario("SetAttribute to root") {
      val elem: Elem = <Order />
      val expected: Elem = <Order A1="1" A2="2" A3="3" />

      val result = elem.transform(
        setAttrs("A1" -> "1", "A2" -> "2", "A3" -> "3")
      )

      assert(result === trim(expected))
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
        $(_ \ "OrderLines") ==> setAttrs("T1" -> "EDITED")
      )

      Console.println(result)
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
        $(_ \ "OrderLines") ==> removeAttrs("T1")
      )

      assert(result \ "OrderLines" \@ "T1" == "")
    }
  }
}
