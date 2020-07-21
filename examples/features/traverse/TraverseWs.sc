import advxml.instances.traverse._
import advxml.syntax.traverse.try_._
import cats.instances.option._

import scala.xml.NodeSeq

//TODO: TO Fix location
import advxml.instances.transform.predicates._

val document =
  <Orders>
    <Order Id="1">
      <OrderLines>
        <OrderLine Id="1" Price="100€"/>
        <OrderLine Id="2" Price="50€"/>
      </OrderLines>
    </Order>
    <Order Id="2">
      <OrderLines>
        <OrderLine Id="1" Price="10€"/>
        <OrderLine Id="2" Price="20€"/>
      </OrderLines>
    </Order>
  </Orders>

val order2Opt: Option[NodeSeq] = (document \? "Order" \? "OrderLines" \? "OrderLine")
    .map(_.filter(attrs(("Id", _ == "2"))))

val order2OptUsingDynamic: Option[NodeSeq] = document.\?*.Order.\?*.OrderLines.\?*.OrderLine.get
    .map(_.filter(attrs(("Id", _ == "2"))))
