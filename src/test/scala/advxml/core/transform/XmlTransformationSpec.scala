package advxml.core.transform

import advxml.test.generators.XmlGenerator
import org.scalacheck.{Arbitrary, Prop, Properties}
import org.scalacheck.Prop.forAll

import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq}

/**
  * Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlTransformationSpec extends Properties("List") {

  implicit val elemGenerator: Arbitrary[NodeSeq] = Arbitrary(
    XmlGenerator
      .xmlNodeGenerator(5)
      .filter(_.children.nonEmpty)
      .map(_.toNode)
  )

  import advxml.implicits._
  import cats.instances.try_._

  property("Prepend") = forAll { (base: NodeSeq, newNode: NodeSeq) =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val rule = $(Function.const(selector.sample.get)) ==> Prepend(newNode)
    val result: Node = base.transform[Try](rule).get.head

    result.asInstanceOf[Node].descendant.contains(newNode)
  }

  property("Append") = forAll { (base: NodeSeq, newNode: NodeSeq) =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val rule = $(Function.const(selector.sample.get)) ==> Append(newNode)
    val result: Node = base.transform[Try](rule).get.head

    result.asInstanceOf[Node].descendant.contains(newNode)
  }

  property("Replace") = forAll { (base: NodeSeq, newNode: NodeSeq) =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val selectedNode = selector.sample.get
    val rule = $(Function.const(selectedNode)) ==> Replace(_ => newNode)
    val result: Node = base.transform[Try](rule).get.head

    result.asInstanceOf[Node].descendant.contains(newNode) &&
    !result.asInstanceOf[Node].descendant.contains(selectedNode)
  }

  property("Remove") = forAll { base: NodeSeq =>
    val selector = XmlGenerator.xmlNodeSelectorGenerator(base.asInstanceOf[Elem])
    val selectedNode = selector.sample.get
    val rule = $(Function.const(selectedNode)) ==> Remove
    val result = base.transform[Try](rule).get.headOption

    result.forall(r => !r.descendant.contains(selectedNode))
  }
}
