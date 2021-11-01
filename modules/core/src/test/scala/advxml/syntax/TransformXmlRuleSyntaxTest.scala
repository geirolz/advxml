package advxml.syntax

import advxml.core.data.error.ZoomFailedException
import cats.data.NonEmptyList
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class TransformXmlRuleSyntaxTest extends AnyFunSuite {

  import advxml.instances.transform._
  import advxml.instances.data._
  import advxml.syntax.transform._
  import advxml.syntax.data._
  import advxml.testUtils.ScalacticXmlEquality._

  import cats.instances.try_._

  test("Transform method on NodeSeq with varargs") {

    val ns: Elem        = <Order></Order>
    val rule1           = root ==> Append(<OrderLine PrimeLineNo="1"/>)
    val rule2           = root ==> Append(<OrderLine PrimeLineNo="2"/>)
    val result: NodeSeq = ns.transform(rule1, rule2).get

    assert(
      <Order>
          <OrderLine PrimeLineNo="1"/>
          <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("Transform method on NodeSeq with List[XmlRule]") {

    val ns: Elem = <Order></Order>
    val rules = List(
      root ==> Append(<OrderLine PrimeLineNo="1"/>),
      root ==> Append(<OrderLine PrimeLineNo="2"/>)
    )
    val result: NodeSeq = ns.transform(rules).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
        <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("Transform method on XmlRule") {

    val ns: Elem        = <Order></Order>
    val rule            = root ==> Append(<OrderLine PrimeLineNo="1"/>)
    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
      </Order> === result
    )
  }

  test("Transform method on List[XmlRule]") {

    val ns: Elem = <Order></Order>
    val rules = List(
      root ==> Append(<OrderLine PrimeLineNo="1"/>),
      root ==> Append(<OrderLine PrimeLineNo="2"/>)
    )
    val result: NodeSeq = rules.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
        <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("conjunction of rules ") {

    val ns: Elem = <Order></Order>
    val rule = (root ==> Append(<OrderLine PrimeLineNo="1"/>))
      .and(root ==> Append(<OrderLine PrimeLineNo="2"/>))

    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
        <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("failing rule orElse other rule") {

    val ns: Elem = <Order></Order>
    val rule =
      (root.Missing ==> Append(<OrderLine PrimeLineNo="1"/>))
        .orElse(root ==> Append(<OrderLine PrimeLineNo="2"/>))

    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("successful rule orElse other rule") {

    val ns: Elem = <Order></Order>
    val rule =
      (root ==> Append(<OrderLine PrimeLineNo="1"/>))
        .orElse(root ==> Append(<OrderLine PrimeLineNo="2"/>))

    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
      </Order> === result
    )
  }

  test("failing rule orElse conjunction of rules") {

    val ns: Elem = <Order></Order>
    val rule =
      (root.Missing ==> Append(<OrderLine PrimeLineNo="1"/>)).orElse {
        (root       ==> Append(<OrderLine PrimeLineNo="1"/>))
          .and(root ==> Append(<OrderLine PrimeLineNo="2"/>))
      }

    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
        <OrderLine PrimeLineNo="2"/>
      </Order> === result
    )
  }

  test("success rule optional") {

    val ns: Elem = <Order></Order>
    val rule     = (root ==> Append(<OrderLine PrimeLineNo="1"/>)).optional

    val result: NodeSeq = rule.transform(ns).get

    assert(
      <Order>
        <OrderLine PrimeLineNo="1"/>
      </Order> === result
    )
  }

  test("failing rule optional") {

    val ns: Elem = <Order></Order>
    val rule     = (root.Missing ==> Append(<OrderLine PrimeLineNo="1"/>)).optional

    val result: NodeSeq = rule.transform(ns).get

    assert(<Order></Order> === result)
  }

  test("Transform XML with empty target") {

    val elem =
      <data>
        <foo>
          <test Id="1"/>
        </foo>
      </data>

    val xmlZoom              = root.foo.test.node
    val result: Try[NodeSeq] = elem.transform(xmlZoom ==> Remove)

    assert(result.isFailure)
    assert(result.failed.get.isInstanceOf[ZoomFailedException])
  }

  test("Transform XML map nested element") {
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
        $.Cat ==> Replace(oldCatNode => {
          oldCatNode.head
            .as[Elem]
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
              (root.bar.test | attrs(k"Id" === "1"))
              ==> Append(<newNode value="x"/>)
            )
            .get
    )
  }

  test("AppendNode in multiple place") {
    assert(
      <Order>
        <OrderLines>
          <OrderLine PrimeLineNo="1">
            <foo value="new"/>
          </OrderLine>
          <OrderLine PrimeLineNo="1" >
            <foo value="new"/>
          </OrderLine>
          <OrderLine PrimeLineNo="2"/>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines>
            <OrderLine PrimeLineNo="1"/>
            <OrderLine PrimeLineNo="1"/>
            <OrderLine PrimeLineNo="2"/>
          </OrderLines>
        </Order>
            .transform(
              root.OrderLines.OrderLine
                .filter(attrs(k"PrimeLineNo" === "1"))
              ==> Append(<foo value="new"/>)
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
              root.OrderLines
              ==> Append(<OrderLine PrimeLineNo="2"/>)
              ==> Append(<OrderLine PrimeLineNo="3"/>)
              ==> Append(<OrderLine PrimeLineNo="4"/>)
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
              root.OrderLines
              ==> Prepend(<OrderLine PrimeLineNo="3"/>)
              ==> Prepend(<OrderLine PrimeLineNo="2"/>)
              ==> Prepend(<OrderLine PrimeLineNo="1"/>)
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
              (root.OrderLines.OrderLine | attrs(k"PrimeLineNo" === "1"))
              ==> Replace(_ => <OrderLine PrimeLineNo="3"/>)
            )
            .get
    )
  }

  test("Replace With same node") {
    val xml = <A><B>1</B></A>
    val result: Try[NodeSeq] = xml.transform(
      (root / "B") ==> Replace(_ => <B>1</B>)
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
              (root.OrderLines.OrderLine | attrs(k"PrimeLineNo" === "2"))
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
              root.OrderLines ==> SetAttrs(k"A1" := "1", k"A2" := "2", k"A3" := "3")
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
              root.OrderLines ==> SetAttrs(k"T1" := "EDITED")
            )
            .get
    )
  }

  test("UpdateAttribute") {
    assert(
      <Order>
        <OrderLines T1="1">
          <OrderLine PrimeLineNo="2"></OrderLine>
        </OrderLines>
      </Order>
        ===
          <Order>
          <OrderLines T1="1">
            <OrderLine PrimeLineNo="1"></OrderLine>
          </OrderLines>
        </Order>
            .transform(
              root.OrderLines.OrderLine ==>
              SetAttrs(ol => {
                val newValue = (ol \@ "PrimeLineNo").toInt + 1
                NonEmptyList.of(k"PrimeLineNo" := newValue)
              })
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
              root.OrderLines ==> RemoveAttrs(_.key == k"T1")
            )
            .get
    )
  }
}
