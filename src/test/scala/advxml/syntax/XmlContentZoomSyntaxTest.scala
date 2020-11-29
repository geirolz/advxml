package advxml.syntax

import advxml.core.transform.XmlContentZoomTest
import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.testUtils.FeatureSpecContract
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try

class XmlContentZoomSyntaxTest extends AnyFeatureSpec with FeatureSpecContract {

  import advxml.instances._
  import advxml.syntax.transform._

  // format: off
  //########################## FLOAT ##########################
  XmlContentZoomTest.Contract[Try](
    "Syntax.Float.Mandatory",
    {
      ContractFuncs(
        attribute         = _./@[Try](_),
        text              = _.textM[Try],
      )
    }
  )(XmlContentZoomTest.TryExtractor).runAll()

  XmlContentZoomTest.Contract[Option](
    "Syntax.Float.Optional",
    {
      ContractFuncs(
        attribute         = _./@[Option](_),
        text              = _.textM[Option],
      )
    }
  )(XmlContentZoomTest.OptionExtractor).runAll()
  // format: on
}
