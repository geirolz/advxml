package com.github.geirolz.advxml.utils

import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult

import scala.xml.parsing.NoBindingFactoryAdapter
import scala.xml.{Elem, Node, Text}

object JavaXmlConverters {

  type JDocument = org.w3c.dom.Document
  type JNode = org.w3c.dom.Node
  type JElement = org.w3c.dom.Element

  lazy val docBuilder: DocumentBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()

  //JAVA => SCALA
  implicit class JavaNodeOps(val inner: JNode) extends AnyVal {
    def asScalaNode: Node = {
      val source = new DOMSource(inner)
      val adapter = new NoBindingFactoryAdapter
      val saxResult = new SAXResult(adapter)
      val transformerFactory = javax.xml.transform.TransformerFactory.newInstance()
      val transformer = transformerFactory.newTransformer()
      transformer.transform(source, saxResult)
      adapter.rootElem
    }
  }

  implicit class JavaDocumentOps(val inner: JDocument) extends AnyVal {
    def asScalaElem: Elem = inner.asScalaNode.asInstanceOf[Elem]
  }

  //SCALA => JAVA
  implicit def nodeExtras(n: Node): NodeExtras = new NodeExtras(n)

  implicit def elemExtras(e: Elem): ElemExtras = new ElemExtras(e)


  class NodeExtras(n: Node) {

    def toJdkNode(doc: JDocument): JNode =
      n match {
        case Elem(_, label, attributes, _, children@_*) =>
          val r = doc.createElement(label)
          for (a <- attributes) {
            r.setAttribute(a.key, a.value.text)
          }
          for (c <- children) {
            r.appendChild(c.toJdkNode(doc))
          }
          r
        case Text(text) => doc.createTextNode(text)
        case _ => doc
      }
  }

  class ElemExtras(e: Elem) extends NodeExtras(e) {
    override def toJdkNode(doc: JDocument): JElement =
      super.toJdkNode(doc).asInstanceOf[JElement]

    def asJavaDocument: JDocument = {
      val doc = docBuilder.newDocument()
      doc.appendChild(toJdkNode(doc))
      doc
    }
  }
}