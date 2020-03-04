package advxml.syntax

import advxml.core.XmlTraverserContractAsserts
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try

class XmlTraverserSyntaxTest extends AnyFeatureSpec with XmlTraverserContractAsserts {

  Feature("XmlTraverserMandatoryFloatOpsForId") {

    import advxml.syntax.traverse._
    import cats.instances.try_._

    Scenario("\\!") {
      mandatory.assertImmediateChild((nodeName, doc) => doc.\![Try](nodeName))
    }

    Scenario("\\\\!") {
      mandatory.assertChildren((nodeName, doc) => doc.\\![Try](nodeName))
    }
    Scenario("\\@!") {
      mandatory.assertAttribute((attrName, doc) => doc.\@![Try](attrName))
    }
    Scenario("!") {
      mandatory.assertText(_.![Try])
    }
    Scenario("|!|") {
      mandatory.assertTrimmedText(_.|!|[Try])
    }
  }

  Feature("XmlTraverserOptionalFloatOpsForId") {

    import advxml.syntax.traverse._
    import cats.instances.option._

    Scenario("\\?") {
      optional.assertImmediateChild((nodeName, doc) => doc.\?[Option](nodeName))
    }

    Scenario("\\\\?") {
      optional.assertChildren((nodeName, doc) => doc.\\?[Option](nodeName))
    }
    Scenario("\\@?") {
      optional.assertAttribute((attrName, doc) => doc.\@?[Option](attrName))
    }
    Scenario("?") {
      optional.assertText(_.?[Option])
    }
    Scenario("|?|") {
      optional.assertTrimmedText(_.|?|[Option])
    }
  }

  Feature("XmlTraverserMandatoryFixedOps") {

    import advxml.syntax.traverse.try_._
    import cats.instances.try_._

    Scenario("\\!") {
      mandatory.assertImmediateChild((nodeName, doc) => doc.\!(nodeName))
    }

    Scenario("\\\\!") {
      mandatory.assertChildren((nodeName, doc) => doc.\\!(nodeName))
    }
    Scenario("\\@!") {
      mandatory.assertAttribute((attrName, doc) => doc.\@!(attrName))
    }
    Scenario("!") {
      mandatory.assertText(_.!)
    }
    Scenario("|!|") {
      mandatory.assertTrimmedText(_.|!|)
    }
  }

  Feature("XmlTraverserOptionalFixedOps") {

    import advxml.syntax.traverse.option._
    import cats.instances.option._

    Scenario("\\?") {
      optional.assertImmediateChild((nodeName, doc) => doc.\?(nodeName))
    }

    Scenario("\\\\?") {
      optional.assertChildren((nodeName, doc) => doc.\\?(nodeName))
    }
    Scenario("\\@?") {
      optional.assertAttribute((attrName, doc) => doc.\@?(attrName))
    }
    Scenario("?") {
      optional.assertText(_.?)
    }
    Scenario("|?|") {
      optional.assertTrimmedText(_.|?|)
    }
  }
}
