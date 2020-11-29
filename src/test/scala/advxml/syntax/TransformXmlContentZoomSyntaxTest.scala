package advxml.syntax

import advxml.core.transform.XmlContentZoomTest
import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class TransformXmlContentZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.instances.convert._
  import advxml.syntax.transform._
  import cats.instances.option._
  import cats.instances.try_._

  // format: off
  XmlContentZoomTest.Contract[Try](
    "Syntax.Try",
    {
      ContractFuncs(
        //attr
        attrFromNs            = _./@[Try](_),
        attrFromM             = _./@[String](_),
        attrFromUnbindedZoom  = (zoom, ns, key) => zoom./@[Try, String](key).apply(ns),
        attrFromBindedZoom    = _./@[Try](_),
        //text
        textFromNs            = _.textM[Try],
        textFromM             = _.textM[String],
        textFromUnbindedZoom  = (zoom, ns) => zoom.textM[Try, String].apply(ns),
        textFromBindedZoom    = _.textM[Try]
      )
    }
  )(XmlContentZoomTest.TryExtractor).runAll()

  XmlContentZoomTest.Contract[Option](
    "Syntax.Option",
    {
      ContractFuncs(
        //attr
        attrFromNs            = _./@[Option](_),
        attrFromM             = _./@[String](_),
        attrFromUnbindedZoom  = (zoom, ns, key) => zoom./@[Option, String](key).apply(ns),
        attrFromBindedZoom    = _./@[Option, String](_),
        //text
        textFromNs            = _.textM[Option],
        textFromM             = _.textM[String],
        textFromUnbindedZoom  = (zoom, ns) => zoom.textM[Option, String].apply(ns),
        textFromBindedZoom    = _.textM[Option]
      )
    }
  )(XmlContentZoomTest.OptionExtractor).runAll()
  // format: on
}
