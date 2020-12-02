object Test {
  import scala.util.Try
  import scala.xml.NodeSeq
  import cats.instances.try_._
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

  val res1 = root.Order.filter(attrs(k"Id" === 1) || attrs(k"Id" === 2)).apply(document).get.nodeSeq

  val result: Try[NodeSeq] = document.transform(
    root.Order
      .find(attrs(k"Id" === 1) || attrs(k"Id" === 2))
      .OrderLines
      .OrderLine
      .find(attrs(k"Id" >= 2)) ==> Append(<Node>new node!</Node>)
  )
}

Test.res1
//Test.result