package advxml.syntax

import advxml.core.transform.actions.{XmlZoomTest, ZoomedNodeSeq}
import advxml.core.transform.actions.XmlZoomTest.ContractFuncs
import advxml.implicits.root
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.Elem

class XmlZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  XmlZoomTest
    .Contract(
      subDesc = "Syntax", {
        import advxml.syntax.transform._
        ContractFuncs(
          immediateDown = (z, n) => z \ n,
          filter = (z, p) => z | p,
          find = (z, p) => z.find(p),
          atIndex = (z, p) => z.atIndex(p),
          head = _.head(),
          last = _.last()
        )
      }
    )
    .runAll()

  test("[XmlZoom.Syntax] - selectDynamic") {

    import cats.instances.try_._

    val doc: Elem =
      <foo>
        <bar>
          <test v="1" />
          <test v="2" />
        </bar>
      </foo>

    val result: Try[ZoomedNodeSeq] = root.bar.test.apply(doc)

    assert(result.get.nodeSeq.size == 2)
    assert(result.get.nodeSeq(0) === <test v="1" />)
    assert(result.get.nodeSeq(1) === <test v="2" />)
  }

  test("[XmlZoom.Syntax] - applyDynamic") {

    import cats.instances.try_._

    val doc: Elem =
      <foo>
        <bar>
          <test v="1" />
          <test v="2" />
        </bar>
      </foo>

    val result: Try[ZoomedNodeSeq] = root.bar.test(1).apply(doc)

    assert(result.get.nodeSeq.size == 1)
    assert(result.get.nodeSeq(0) === <test v="2" />)
  }
}
