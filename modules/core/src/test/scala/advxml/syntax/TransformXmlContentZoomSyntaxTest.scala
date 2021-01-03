package advxml.syntax

import advxml.core.transform.XmlContentZoomTest
import advxml.core.transform.XmlContentZoomTest.ContractFuncs
import advxml.testUtils.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class TransformXmlContentZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import advxml.syntax.transform._
  import cats.instances.try_._

  // format: off
  XmlContentZoomTest.Contract[Try](
    "Syntax.WithString",
    {
      ContractFuncs[Try](
        //label
        label                 = ns => ns.label,
        labelFromBindedZoom   = zoom => zoom.label.extract[Try],
        labelFromZoom         = (zoom, ns) => zoom.label(ns).extract[Try],
        //attr
        attr                  = (ns, key) => ns.attr(key),
        attrFromBindedZoom    = (zoom, key) => zoom.attr(key).extract[Try],
        attrFromZoom          = (zoom, ns, key) => zoom.attr(ns, key).extract[Try],
        //content
        content               = ns => ns.content,
        contentFromBindedZoom = zoom => zoom.content.extract[Try],
        contentFromZoom       = (zoom, ns) => zoom.content(ns).extract[Try]
      )
    }
  ).runAll()
  // format: on
}
