package advxml.syntax

import advxml.core.XmlTraverserTest
import advxml.core.XmlTraverserTest.ContractFuncs
import advxml.test.FeatureSpecContract
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try

class XmlTraverserSyntaxTest extends AnyFeatureSpec with FeatureSpecContract {

  import advxml.syntax.traverse._

  // format: off
  //########################## FLOAT ##########################
  XmlTraverserTest.Contract[Try](
    "Syntax.Float.Mandatory",
    {
      ContractFuncs(
        immediateChild    = (doc, nodeName) => doc.\![Try](nodeName),
        children          = (doc, nodeName) => doc.\\![Try](nodeName),
        attribute         = (doc, attrName) => doc.\@![Try](attrName),
        text              = _.![Try],
        trimmedText       = _.|!|[Try]
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Syntax.Float.Optional",
    {
      ContractFuncs(
        immediateChild      = (doc, nodeName) => doc.\?[Option](nodeName),
        children            = (doc, nodeName) => doc.\\?[Option](nodeName),
        attribute           = (doc, attrName) => doc.\@?[Option](attrName),
        text                = _.?[Option],
        trimmedText         = _.|?|[Option]
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()


  //########################## FIXED ##########################
  XmlTraverserTest.Contract[Try](
    "Syntax.Fixed.Mandatory",
    {
      import advxml.syntax.traverse.try_._
      ContractFuncs(
        immediateChild      = (doc, nodeName) => doc.\!(nodeName),
        children            = (doc, nodeName) => doc.\\!(nodeName),
        attribute           = (doc, attrName) => doc.\@!(attrName),
        text                = _.!,
        trimmedText         = _.|!|
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Syntax.Fixed.Optional",
    {
      import advxml.syntax.traverse.option._
      ContractFuncs(
        immediateChild      = (doc, nodeName) => doc.\?(nodeName),
        children            = (doc, nodeName) => doc.\\?(nodeName),
        attribute           = (doc, attrName) => doc.\@?(attrName),
        text                = _.?,
        trimmedText         = _.|?|
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()
  // format: on
}
