//package com.dg.advxml
//
//import org.scalatest.FeatureSpec
//
//import scala.xml.Elem
//
//class AdvXmlTest extends FeatureSpec  {
//
//  import com.dg.advxml.AdvXml._
//  import org.scalatest.StreamlinedXmlEquality._
//
//  feature("Xml manipulation: Filters") {
//    scenario("Filter By Attribute") {
//      val elem: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//          <OrderLine PrimeLineNo="2" />
//          <OrderLine PrimeLineNo="3" />
//        </OrderLines>
//      </Order>
//
//      val expected: Elem = <OrderLine PrimeLineNo="1" />
//      val result = elem \ "OrderLines" \ "OrderLine" filter attr("PrimeLineNo", "1")
//
//      result === expected
//    }
//  }
//
//  feature("Xml manipulation: Nodes") {
//
//    scenario("AppendNode") {
//      val elem: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//        </OrderLines>
//      </Order>
//
//      val expected: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//          <OrderLine PrimeLineNo="2" />
//        </OrderLines>
//      </Order>
//
//
//      val result = elem.transform(
//        $(_ \ "OrderLines")(append(<OrderLine PrimeLineNo="2" />))
//      )
//
//      assert(result === expected)
//    }
//
//    scenario("ReplaceNode") {
//      val elem: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//        </OrderLines>
//      </Order>
//
//      val expected: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="4" />
//        </OrderLines>
//      </Order>
//
//      val result = elem.transform(
//        $(_ \ "OrderLines" \ "OrderLine" filter attr("PrimeLineNo", "1"))(
//          replace(<OrderLine PrimeLineNo="4" />)
//        )
//      )
//
//      assert(result === expected)
//    }
//
//    scenario("RemoveNode") {
//      val elem: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//          <OrderLine PrimeLineNo="2" />
//        </OrderLines>
//      </Order>
//
//      val expected: Elem = <Order>
//        <OrderLines>
//          <OrderLine PrimeLineNo="2" />
//        </OrderLines>
//      </Order>
//
//      val result = elem.transform(
//        $(_ \ "OrderLines" \ "OrderLine" filter attr("PrimeLineNo", "1"))(remove)
//      )
//
//      assert(result === expected)
//    }
//
//    scenario("AppendNode to Root"){
//      val elem: Elem = <OrderLines />
//
//      val expected: Elem =
//        <OrderLines>
//          <OrderLine PrimeLineNo="1" />
//        </OrderLines>
//
//
//      val result = elem.transformCurrent(
//        append(<OrderLine PrimeLineNo="1" />)
//      )
//
//      assert(result === expected)
//    }
//  }
//
//  feature("Xml manipulation: Attributes") {
//
//    scenario("SetAttribute") {
//      val elem: Elem = <Order><OrderLines /></Order>
//      val expected: Elem = <Order><OrderLines A1="1" A2="2" A3="3" /></Order>
//
//      val result = elem.transform(
//        $(_ \ "OrderLines")(
//          setAttr("A1", "1"),
//          setAttr("A2", "2"),
//          setAttr("A3", "3")
//        )
//      )
//
//      assert(result === expected)
//    }
//
//    scenario("SetAttribute to root") {
//      val elem: Elem = <Order />
//      val expected: Elem = <Order A1="1" A2="2" A3="3" />
//
//      val result = elem.transform(
//        current(
//          setAttr("A1", "1"),
//          setAttr("A2", "2"),
//          setAttr("A3", "3")
//        )
//      )
//
//      assert(result === expected)
//    }
//
//    //
//    //    scenario("ReplaceAttribute") {
//    //      val elem: Elem = <Order>
//    //        <OrderLines T1="1">
//    //          <OrderLine PrimeLineNo="1"></OrderLine>
//    //          <OrderLine PrimeLineNo="2"></OrderLine>
//    //          <OrderLine PrimeLineNo="3"></OrderLine>
//    //        </OrderLines>
//    //      </Order>
//    //
//    //      val result = (elem.asRoot \ "OrderLines").modify(
//    //        setAttr("T1", "EDITED")
//    //      )
//    //
//    //      assert(result \ "OrderLines" \@ "T1" == "EDITED")
//    //    }
//    //
//    //    scenario("RemoveAttribute") {
//    //      val elem: Elem = <Order>
//    //        <OrderLines T1="1">
//    //          <OrderLine PrimeLineNo="1"></OrderLine>
//    //          <OrderLine PrimeLineNo="2"></OrderLine>
//    //          <OrderLine PrimeLineNo="3"></OrderLine>
//    //        </OrderLines>
//    //      </Order>
//    //
//    //      val result = (elem.asRoot \ "OrderLines").modify(
//    //        removeAttr("T1")
//    //      )
//    //
//    //      assert(result \ "OrderLines" \@? "T1" isEmpty)
//    //    }
//  }
//}
