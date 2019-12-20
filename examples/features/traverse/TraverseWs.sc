import advxml.syntax.nestedMap._
import advxml.syntax.traverse.try_._

import scala.util.Try
import scala.xml.NodeSeq
import cats.instances.try_._
import cats.instances.option._

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

val order2Opt : Try[Option[NodeSeq]] = (document \? "Order")
  .nMap(_.filter(attrs(("Id", _ == "2"))))