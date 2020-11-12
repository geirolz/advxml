package advxml.syntax

import advxml.core.transform.actions.XmlZoomTest
import advxml.core.transform.actions.XmlZoomTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

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
}
