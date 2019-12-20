import advxml.instances.transform._
import advxml.syntax.transform._
import cats.instances.try_._

import scala.util.Try
import scala.xml.NodeSeq

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


val result: Try[NodeSeq] = document.transform($(
  _ \ "Order" filter attrs(("Id", _ == "1")),
  _ \ "OrderLines" \ "OrderLine" filter attrs("Id" -> (_ == "2"))
) ==> Append(<Node>new node!</Node>))

