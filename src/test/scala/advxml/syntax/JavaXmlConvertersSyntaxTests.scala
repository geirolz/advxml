package advxml.syntax

import advxml.core.utils.JavaXmlConvertersTest
import advxml.core.utils.JavaXmlConvertersTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

class JavaXmlConvertersSyntaxTests extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.javaConverters._

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
