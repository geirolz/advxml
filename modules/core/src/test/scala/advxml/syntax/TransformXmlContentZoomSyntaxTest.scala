package advxml.syntax

import advxml.transform.XmlContentZoomTest
import advxml.transform.XmlContentZoomTest.ContractFuncs
import advxml.testing.FunSuiteContract
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

class TransformXmlContentZoomSyntaxTest extends AnyFunSuite with FunSuiteContract {

  import cats.instances.try_.*
  import advxml.implicits.*

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
