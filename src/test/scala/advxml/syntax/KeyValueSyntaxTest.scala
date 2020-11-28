package advxml.syntax

import advxml.core.KeyValueTest
import advxml.core.KeyValueTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

class KeyValueSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.instances.convert._

  KeyValueTest
    .Contract(
      // format: off
      f = ContractFuncs(
        equals        = _ === _,
        notEquals     = _ =!= _,
        lessThen      = _ < _,
        lessEqThen    = _ <= _,
        greaterThen   = _ > _,
        greaterEqThen = _ >= _
      )
      // format: on
    )
    .runAll()
}
