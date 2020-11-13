
object Test {

  import cats.instances.option._
  import scala.xml.NodeSeq
  import advxml.implicits._

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
    .map(_.filter(attrs(k"Id" === "2")))

  val order2OptUsingDynamic: Option[NodeSeq] = document.\?*.Order.OrderLines.get
    .filterChild(attrs(k"Id" === "2"))
}

Test.order2Opt

Test.order2OptUsingDynamic