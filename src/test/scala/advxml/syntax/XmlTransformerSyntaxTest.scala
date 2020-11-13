package advxml.syntax

import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class XmlTransformerSyntaxTest extends AnyFunSuite {

  import advxml.instances._
  import advxml.instances.transform._
  import advxml.syntax.transform._
  import advxml.testUtils.ScalacticXmlEquality._
  import cats.instances.try_._

  test("Transform XML with empty target") {

    val elem = <data>
      <foo>
        <test Id="1"/>
      </foo>
    </data>

    val xmlZoom = root \ "test" \ "unknown" \ "node"
    val result: Try[NodeSeq] = elem.transform(xmlZoom ==> Remove)

    assert(result.isFailure)
//    assert(result.failed.get.isInstanceOf[EmptyTargetException])//TODO
  }

  test("Transform XML map nested element") {
    import advxml.implicits._
    import cats.instances.try_._

    val pets =
      <Pet>
        <Cat a="TEST">
          <Kitty>small</Kitty>
          <Kitty>big</Kitty>
          <Kitty>large</Kitty>
        </Cat>
        <Dog>
          <Kitty>large</Kitty>
        </Dog>
      </Pet>

    val result: NodeSeq = pets
      .transform(
        (> \ "Cat") ==> Replace(oldCatNode => {
          oldCatNode.head
            .asPure[Elem]
            .copy(child = oldCatNode \ "Kitty" map (k => <c>
            {k.text}
          </c>))
        })
      )
      .get

    assert(
      result ===
        <Pet>
        <Cat a="TEST">
          <c>small</c>
          <c>big</c>
          <c>large</c>
        </Cat>
        <Dog>
          <Kitty>large</Kitty>
        </Dog>
      </Pet>
    )
  }

  test("Transform XML with equals nodes in different paths") {
    assert(
      <data>
        <foo>
          <test Id="1" />
        </foo>
        <bar>
          <test Id="1"><newNode value="x"/></test>
        </bar>
      </data>
        ===
          <data>
          <foo>
            <test Id="1"/>
          </foo>
          <bar>
            <test Id="1"/>
          </bar>
        </data>
            .transform(
              (root \ "bar" \ "test" filter attrs(k"Id" === "1"))
              ==> Append(<newNode value="x"/>)
            )
            .get
    )
  }

  test("PrependNode") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1"/>
          <OrderLine PrimeLineNo="2"/>
          <OrderLine PrimeLineNo="3"/>
          <OrderLine PrimeLineNo="4"/>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines>
            <OrderLine PrimeLineNo="4"/>
          </OrderLines>
        </Order>
            .transform(
              (root \ "OrderLines")
              ==> Prepend(<OrderLine PrimeLineNo="3"/>)
              ==> Prepend(<OrderLine PrimeLineNo="2"/>)
              ==> Prepend(<OrderLine PrimeLineNo="1"/>)
            )
            .get
    )
  }

  test("AppendNode") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1"/>
          <OrderLine PrimeLineNo="2"/>
          <OrderLine PrimeLineNo="3"/>
          <OrderLine PrimeLineNo="4"/>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines>
            <OrderLine PrimeLineNo="1"/>
          </OrderLines>
        </Order>
            .transform(
              (root \ "OrderLines")
              ==> Append(<OrderLine PrimeLineNo="2"/>)
              ==> Append(<OrderLine PrimeLineNo="3"/>)
              ==> Append(<OrderLine PrimeLineNo="4"/>)
            )
            .get
    )
  }

  test("ReplaceNode") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="3" />
          <OrderLine PrimeLineNo="2" />
          <OrderLine PrimeLineNo="3" />
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines>
            <OrderLine PrimeLineNo="1"/>
            <OrderLine PrimeLineNo="2"/>
            <OrderLine PrimeLineNo="3"/>
          </OrderLines>
        </Order>
            .transform(
              (root \ "OrderLines" \ "OrderLine" | attrs(k"PrimeLineNo" === "1"))
              ==> Replace(_ => <OrderLine PrimeLineNo="3"/>)
            )
            .get
    )
  }

  test("Replace With same node") {
    val xml = <A><B>1</B></A>
    val result: Try[NodeSeq] = xml.transform(
      (root \ "B") ==> Replace(_ => <B>1</B>)
    )

    assert(result.get === <A><B>1</B></A>)
  }

  test("RemoveNode") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1" />
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines>
            <OrderLine PrimeLineNo="1"/>
            <OrderLine PrimeLineNo="2"/>
          </OrderLines>
        </Order>
            .transform(
              (root \ "OrderLines" \ "OrderLine" | attrs(k"PrimeLineNo" === "2"))
              ==> Remove
            )
            .get
    )
  }

  test("RemoveNode root") {
    val result = <Order>
      <OrderLines>
        <OrderLine PrimeLineNo="1"/>
        <OrderLine PrimeLineNo="2"/>
      </OrderLines>
    </Order>.transform(root ==> Remove)

    assert(result.get.isEmpty)
  }

  test("AppendNode to Root") {
    assert(
      <OrderLines>
        <OrderLine PrimeLineNo="1"/>
      </OrderLines>
        ===
          <OrderLines/>
            .transform[Try](
              root ==> Append(<OrderLine PrimeLineNo="1"/>)
            )
            .get
    )
  }

  test("SetAttribute") {
    assert(
      <Order>
        <OrderLines A1="1" A2="2" A3="3"/>
      </Order>
        ===
          <Order>
          <OrderLines/>
        </Order>
            .transform(
              root \ "OrderLines" ==> SetAttrs(k"A1" := "1", k"A2" := "2", k"A3" := "3")
            )
            .get
    )
  }

  test("SetAttribute to root") {
    assert(
      <Order A1="1" A2="2" A3="3"/> === <Order/>
        .transform(
          root ==> SetAttrs(k"A1" := "1", k"A2" := "2", k"A3" := "3")
        )
        .get
    )
  }

  test("ReplaceAttribute") {
    assert(
      <Order>
        <OrderLines T1="EDITED">
          <OrderLine PrimeLineNo="1"></OrderLine>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines T1="1">
            <OrderLine PrimeLineNo="1"></OrderLine>
          </OrderLines>
        </Order>
            .transform(
              root \ "OrderLines" ==> SetAttrs(k"T1" := "EDITED")
            )
            .get
    )
  }

  test("RemoveAttribute") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1"></OrderLine>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines T1="1">
            <OrderLine PrimeLineNo="1"></OrderLine>
          </OrderLines>
        </Order>
            .transform(
              root \ "OrderLines" ==> RemoveAttrs(_.key == k"T1")
            )
            .get
    )
  }
}
