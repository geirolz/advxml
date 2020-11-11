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
          immediateDownAction = (z, n) => z \ n,
          filterAction = (z, p) => z | p
        )
      }
    )
    .runAll()
}
