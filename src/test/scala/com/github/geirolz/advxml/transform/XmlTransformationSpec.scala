package com.github.geirolz.advxml.transform

import com.github.geirolz.advxml.generators.XmlGenerator
import com.github.geirolz.advxml.generators.XmlGenerator.BasicXmlNode
import com.github.geirolz.advxml.transform.actions.XmlZoom
import org.scalacheck.{Arbitrary, Gen, Properties}
import org.scalacheck.Prop.forAll
import org.scalatest.FunSuiteLike

import scala.xml.{Elem, Node, NodeSeq}

/**
  * Advxml
  * Created by geirolad on 12/07/2019.
  *
  * @author geirolad
  */
object XmlTransformationSpec extends Properties("List") with FunSuiteLike {

  implicit val elemGenerator: Arbitrary[XmlGenerator.BasicXmlNode] = Arbitrary(XmlGenerator.xmlNodeGenerator)

  import cats.instances.try_._
  import com.github.geirolz.advxml.all._

  property("") = forAll { (root: BasicXmlNode, newNode: BasicXmlNode) =>
    val zoomGen: Gen[XmlZoom] = XmlGenerator.xmlZoomGenerator(root.toNode.asInstanceOf[Elem])
    val zoom = zoomGen.sample.get
    val rootNode = zoom(root.toNode)
    val newScalaNode: Node = newNode.toNode
    val result: NodeSeq = rootNode.transform(Append(newScalaNode)).get

    //TODO Improve this assert
    zoom(result).size == newScalaNode.size
  }
}
