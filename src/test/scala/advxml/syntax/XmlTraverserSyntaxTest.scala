package advxml.syntax

import advxml.core.XmlTraverserTest
import advxml.core.XmlTraverserTest.ContractFuncs
import advxml.testUtils.FeatureSpecContract
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.Try

class XmlTraverserSyntaxTest extends AnyFeatureSpec with FeatureSpecContract {

  import advxml.instances.traverse._
  import advxml.syntax.traverse._

  // format: off
  //########################## FLOAT ##########################
  XmlTraverserTest.Contract[Try](
    "Syntax.Float.Mandatory",
    {
      import cats.instances.try_._
      ContractFuncs[Try](
        immediateChild    = (doc, nodeName) => doc.\![Try](nodeName),
        children          = (doc, nodeName) => doc.\\![Try](nodeName),
        attribute         = (doc, attrName) => doc.\@![Try](attrName),
        text              = _.![Try],
        trimmedText       = _.|!|[Try],
        atIndex           = (doc, idx) => doc.childAtIndex[Try](idx),
        head              = _.headChild[Try],
        last              = _.lastChild[Try],
        tail              = _.tailChild[Try],
        find              = (doc, p) => doc.findChild[Try](p),
        filter            = (doc, p) => doc.filterChild[Try](p)
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Syntax.Float.Optional",
    {
      import cats.instances.option._
      ContractFuncs[Option](
        immediateChild      = (doc, nodeName) => doc.\?[Option](nodeName),
        children            = (doc, nodeName) => doc.\\?[Option](nodeName),
        attribute           = (doc, attrName) => doc.\@?[Option](attrName),
        text                = _.?[Option],
        trimmedText         = _.|?|[Option],
        atIndex           = (doc, idx) => doc.childAtIndex[Option](idx),
        head              = _.headChild[Option],
        last              = _.lastChild[Option],
        tail              = _.tailChild[Option],
        find              = (doc, p) => doc.findChild[Option](p),
        filter            = (doc, p) => doc.filterChild[Option](p)
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()
  
  //########################## FIXED ##########################
  XmlTraverserTest.Contract[Try](
    "Syntax.Fixed.Mandatory",
    {
      import advxml.syntax.traverse.try_._
      import cats.instances.try_._
      ContractFuncs[Try](
        immediateChild      = (doc, nodeName) => doc.\!(nodeName),
        children            = (doc, nodeName) => doc.\\!(nodeName),
        attribute           = (doc, attrName) => doc.\@!(attrName),
        text                = _.!,
        trimmedText         = _.|!|,
        atIndex             = (doc, idx) => doc.childAtIndex(idx),
        head                = _.headChild,
        last                = _.lastChild,
        tail                = _.tailChild,
        find                = (doc, p) => doc.findChild(p),
        filter              = (doc, p) => doc.filterChild(p)
      )
    }
  )(XmlTraverserTest.TryExtractor).runAll()

  XmlTraverserTest.Contract[Option](
    "Syntax.Fixed.Optional",
    {
      import advxml.syntax.traverse.option._
      import cats.instances.option._
      ContractFuncs[Option](
        immediateChild      = (doc, nodeName) => doc.\?(nodeName),
        children            = (doc, nodeName) => doc.\\?(nodeName),
        attribute           = (doc, attrName) => doc.\@?(attrName),
        text                = _.?,
        trimmedText         = _.|?|,
        atIndex             = (doc, idx) => doc.childAtIndex(idx),
        head                = _.headChild,
        last                = _.lastChild,
        tail                = _.tailChild,
        find                = (doc, p) => doc.findChild(p),
        filter              = (doc, p) => doc.filterChild(p)
      )
    }
  )(XmlTraverserTest.OptionExtractor).runAll()
  // format: on
}
