package advxml.syntax

import advxml.core.transform.actions.XmlZoomTest
import advxml.core.transform.actions.XmlZoomTest.ContractFuncs
import advxml.core.transform.XmlZoom.root
import advxml.core.transform.XmlZoomResult
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.Elem

class TransformXmlZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.transform._

  XmlZoomTest
    .Contract(
      subDesc = "Syntax", {
        // format: off
        ContractFuncs(
          immediateDown = (z, n) => z / n,
          filter = (z, p) => z | p,
          find = (z, p) => z.find(p),
          atIndex = (z, p) => z.atIndex(p),
          head = _.head(),
          last = _.last()
        )
        // format: off
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

    val result: Try[XmlZoomResult] = root.bar.test.run(doc)

    assert(result.get.nodeSeq.size == 2)
    assert(result.get.nodeSeq.head === <test v="1" />)
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

    val result: Try[XmlZoomResult] = root.bar.test(1).run(doc)

    assert(result.get.nodeSeq.size == 1)
    assert(result.get.nodeSeq.head === <test v="2" />)
  }
}
