package advxml.syntax

import advxml.core.transform.XmlContentZoomTest
import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class TransformXmlContentZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.instances.convert._
  import advxml.syntax.transform._
  import cats.instances.try_._

  // format: off
  XmlContentZoomTest.Contract[Try](
    "Syntax.WithConverter",
    {
      ContractFuncs(
        //attr
        attrFromNs            = _./@[Try, String](_),
        attrFromM             = _./@[String](_),
        attrFromUnbindedZoom  = (zoom, ns, key) => zoom./@[Try, String](key).apply(ns),
        attrFromBindedZoom    = _./@[Try, String](_),
        //text
        textFromNs            = _.textM[Try, String],
        textFromM             = _.textM[String],
        textFromUnbindedZoom  = (zoom, ns) => zoom.textM[Try, String].apply(ns),
        textFromBindedZoom    = _.textM[Try, String]
      )
    }
  )(XmlContentZoomTest.TryExtractor).runAll()

  
  XmlContentZoomTest.Contract[Try](
    "Syntax.WithString",
    {
      ContractFuncs(
        //attr
        attrFromNs            = _./@[Try](_),
        attrFromM             = _./@[String](_),
        attrFromUnbindedZoom  = (zoom, ns, key) => zoom./@[Try](key).apply(ns),
        attrFromBindedZoom    = _./@[Try](_),
        //text
        textFromNs            = _.textM[Try],
        textFromM             = _.textM,
        textFromUnbindedZoom  = (zoom, ns) => zoom.textM[Try].apply(ns),
        textFromBindedZoom    = _.textM[Try]
      )
    }
  )(XmlContentZoomTest.TryExtractor).runAll()
  // format: on
}
