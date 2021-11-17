package advxml.syntax

import advxml.utils.JavaXmlConvertersTest
import advxml.utils.JavaXmlConvertersTest.ContractFuncs
import advxml.testing.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

class JavaXmlConvertersSyntaxTests extends AnyFunSuite with FunSuiteContract {

  import advxml.implicits.*

  // format: off
  JavaXmlConvertersTest
    .Contract(
      f = ContractFuncs(
        asJava          = _.asJava,
        asJavaWithNode  =  (node, document) => node.asJava(document),
        jNodeAsScala    = _.asScala,
        jDocAsScala     = _.asScala,
        toPrettyString  = _.toPrettyString()
      )
    )
    .runAll()
  // format: on
}
