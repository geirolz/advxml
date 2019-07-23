package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.generators.XmlGenerator
import org.scalacheck.{Arbitrary, Properties}
import org.scalacheck.Prop.forAll

import scala.xml.{Elem, Node, NodeSeq}

/**
  * Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlTransformationSpec extends Properties("List") {

  implicit val elemGenerator: Arbitrary[NodeSeq] = Arbitrary(
    XmlGenerator.xmlNodeGenerator.filter(_.children.nonEmpty).map(_.toNode)
  )

  import cats.instances.try_._
  import com.github.geirolz.advxml.all._

  property("Append") = forAll { (base: NodeSeq, newNode: NodeSeq) =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val rule = $(Function.const(selector.sample.get)) ==> Append(newNode)
    val result: Node = base.transform(rule).get.head

    result.asInstanceOf[Node].descendant.contains(newNode)
  }

  property("Replace") = forAll { (base: NodeSeq, newNode: NodeSeq) =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val selectedNode = selector.sample.get
    val rule = $(Function.const(selectedNode)) ==> Replace(newNode)
    val result: Node = base.transform(rule).get.head

    result.asInstanceOf[Node].descendant.contains(newNode)
    !result.asInstanceOf[Node].descendant.contains(selectedNode)
  }

  property("Remove") = forAll { base: NodeSeq =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val selectedNode = selector.sample.get
    val rule = $(Function.const(selectedNode)) ==> Remove
    val result = base.transform(rule).get.headOption

    result.forall(r => !r.descendant.contains(selectedNode))
  }
}
