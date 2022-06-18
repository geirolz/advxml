package advxml.xpath

import advxml.transform.*
import advxml.implicits.*
import cats.instances.try_.*
import org.scalactic.Equality
import org.scalatest.OptionValues
import org.scalatest.TryValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Try
import scala.xml.NodeSeq

class XmlZoomXPathSupportTest extends AnyFunSuite with Matchers with OptionValues with TryValues {
  implicit private val nodeSeqEquality: Equality[NodeSeq] = (a: NodeSeq, b: Any) =>
    b match {
      case ns: NodeSeq => a.normalizedEquals(ns)
      case _           => false
    }

  test("Absolute path") {
    val zoom = XmlZoom.fromXPath("/root")

    zoom.toOption.value shouldBe XmlZoom.$.down("root")
  }

  test("Absolute path with selector") {
    val zoom = XmlZoom.fromXPath("""/root/@id""")

    zoom shouldBe Symbol("invalid")
  }

  test("Absolute path with index") {
    val zoom = XmlZoom.fromXPath("""/root[1]""")

    zoom.toOption.value shouldBe XmlZoom.$.down("root").atIndex(1)
  }

  test("Absolute path with attribute selector") {
    val zoom = XmlZoom.fromXPath("/root[@id='1']")

    zoom shouldBe Symbol("valid")

    val data = <wrapper><root></root><root id="1"></root></wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]

    sut.success.value shouldEqual <root id="1"></root>
  }

  test("Absolute path with text()") {
    val zoom = XmlZoom.fromXPath("/root[text()='mew']")

    zoom shouldBe Symbol("valid")

    val data = <wrapper><root id="1">pew</root><root id="2">mew</root></wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]

    sut.success.value shouldEqual <root id="2">mew</root>
  }

  test("Absolute path with child predicate (>)") {
    val zoom = XmlZoom.fromXPath("/root/child[value>4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]

    sut.success.value shouldEqual <value>5</value>
  }

  test("Absolute path with child predicate (>=)") {
    val zoom = XmlZoom.fromXPath("/root/child[value>=4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>4</value><value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Absolute path with child predicate (=)") {
    val zoom = XmlZoom.fromXPath("/root/child[value=4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]

    sut.success.value shouldEqual <value>4</value>
  }

  test("Absolute path with child predicate (!=)") {
    val zoom = XmlZoom.fromXPath("/root/child[value!=4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value><value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Absolute path with child predicate (<)") {
    val zoom = XmlZoom.fromXPath("/root/child[value<4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value>

    sut.success.value shouldEqual expected
  }

  test("Absolute path with child predicate (<=)") {
    val zoom = XmlZoom.fromXPath("/root/child[value<=4]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value><value>4</value>

    sut.success.value shouldEqual expected
  }

  test("Absolute path with child predicate (text() =)") {
    val zoom = XmlZoom.fromXPath("/root/child[value/text()='4']/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]
    sut.success.value shouldEqual <value>4</value>
  }

  test("Absolute path with child predicate (contains(text()))") {
    val zoom = XmlZoom.fromXPath("""/root/child[value/contains(text(),'ar')]/value""")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>foo</value></child>
          <child><value>bar</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]
    sut.success.value shouldEqual <value>bar</value>
  }

  test("Absolute path with child predicate (starts-with(text()))") {
    val zoom = XmlZoom.fromXPath("""/root/child[value/starts-with(text(),'ba')]/value""")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>foo</value></child>
          <child><value>bar</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]
    sut.success.value shouldEqual <value>bar</value>
  }

  test("Absolute path with child predicate (ends-with(text()))") {
    val zoom = XmlZoom.fromXPath("""/root/child[value/ends-with(text(),'o')]/value""")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>foo</value></child>
          <child><value>bar</value></child>
        </root>
      </wrapper>

    val sut = zoom.toOption.get.bind(data).run[Try]
    sut.success.value shouldEqual <value>foo</value>
  }

  test("Path with wildcard") {
    val zoom = XmlZoom.fromXPath("/root/*/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value><value>4</value><value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Last child") {
    val zoom = XmlZoom.fromXPath("/root/child[last()]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Or logic") {
    val zoom = XmlZoom.fromXPath("/root/child[@id='1' or @id='2']/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child id="1"><value>3</value></child>
          <child id="2"><value>4</value></child>
          <child id="3"><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value><value>4</value>

    sut.success.value shouldEqual expected
  }

  test("And logic") {
    val zoom = XmlZoom.fromXPath("/root/child[value>3 and value<5]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child><value>3</value></child>
          <child><value>4</value></child>
          <child><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>4</value>

    sut.success.value shouldEqual expected
  }

  test("Attribute contains") {
    val zoom = XmlZoom.fromXPath("/root/child[contains(@id, 'ba')]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child id="foo"><value>3</value></child>
          <child id="bar"><value>4</value></child>
          <child id="baz"><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>4</value><value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Attribute starts-with") {
    val zoom = XmlZoom.fromXPath("/root/child[starts-with(@id, 'ba')]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child id="foo"><value>3</value></child>
          <child id="bar"><value>4</value></child>
          <child id="baz"><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>4</value><value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Attribute ends-with") {
    val zoom = XmlZoom.fromXPath("/root/child[ends-with(@id, 'az')]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child id="foo"><value>3</value></child>
          <child id="bar"><value>4</value></child>
          <child id="baz"><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>5</value>

    sut.success.value shouldEqual expected
  }

  test("Attribute not contains") {
    val zoom = XmlZoom.fromXPath("/root/child[not(contains(@id, 'ba'))]/value")

    zoom shouldBe Symbol("valid")

    val data =
      <wrapper>
        <root>
          <child id="foo"><value>3</value></child>
          <child id="bar"><value>4</value></child>
          <child id="baz"><value>5</value></child>
        </root>
      </wrapper>

    val sut               = zoom.toOption.get.bind(data).run[Try]
    val expected: NodeSeq = <value>3</value>

    sut.success.value shouldEqual expected
  }

  test("Union") {
    val zoom = XmlZoom.fromXPath("/root/child[@id='1']/value | /root/child[@id='2']/value")

    zoom shouldBe Symbol("invalid")
  }

  test("Traversal path") {
    val zoom = XmlZoom.fromXPath("//root")

    zoom shouldBe Symbol("invalid")
  }
}
