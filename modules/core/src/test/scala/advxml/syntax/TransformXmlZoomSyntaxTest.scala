package advxml.syntax

import advxml.transform.XmlZoom.root
import advxml.transform.XmlZoomTest
import advxml.transform.XmlZoomTest.ContractFuncs
import advxml.testing.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class TransformXmlZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.implicits.*

  XmlZoomTest
    .Contract(
      subDesc = "Syntax", {
        // format: off
        ContractFuncs(
          down   = (z, n) => z / n,
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

    import cats.instances.try_.*

    val doc: Elem =
      <foo>
        <bar>
          <test v="1" />
          <test v="2" />
        </bar>
      </foo>

    val result: Try[NodeSeq] = root.bar.test.run[Try](doc)

    assert(result.get.size == 2)
    assert(result.get.head === <test v="1" />)
    assert(result.get.apply(1) === <test v="2" />)
  }

  test("[XmlZoom.Syntax] - applyDynamic") {

    import cats.instances.try_.*

    val doc: Elem =
      <foo>
        <bar>
          <test v="1" />
          <test v="2" />
        </bar>
      </foo>

    val result: Try[NodeSeq] = root.bar.test(1).run[Try](doc)

    assert(result.get.size == 1)
    assert(result.get.head === <test v="2" />)
  }
}
