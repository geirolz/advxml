package advxml.core.utils

import java.io.StringWriter

import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import javax.xml.transform.{OutputKeys, Transformer, TransformerFactory}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamResult

import scala.xml.{Elem, Node, Text}
import scala.xml.parsing.NoBindingFactoryAdapter

object JavaXmlConverters {

  type JDocument = org.w3c.dom.Document
  type JNode = org.w3c.dom.Node
  type JElement = org.w3c.dom.Element

  object FromScala {

    lazy val documentBuilder: DocumentBuilder = DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()

    def asJava(elem: Elem): JDocument = {
      val doc = documentBuilder.newDocument()
      doc.appendChild(asJava(elem.asInstanceOf[Node], doc))
      doc
    }

    def asJava(node: Node, doc: JDocument): JNode =
      node match {
        case Elem(_, label, attributes, _, children @ _*) =>
          val r = doc.createElement(label)
          for (a <- attributes) {
            r.setAttribute(a.key, a.value.text)
          }
          for (c <- children) {
            r.appendChild(asJava(c, doc))
          }
          r
        case Text(text) => doc.createTextNode(text)
        case _          => doc
      }

  }

  object FromJava {

    lazy val defaultTransformer: Transformer = {
      val transformer = TransformerFactory.newInstance.newTransformer
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      transformer.setOutputProperty(OutputKeys.METHOD, "xml")
      transformer.setOutputProperty(OutputKeys.INDENT, "no")
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
      transformer
    }

    def asScala(jNode: JNode): Node = {
      val source = new DOMSource(jNode)
      val adapter = new NoBindingFactoryAdapter
      val saxResult = new SAXResult(adapter)
      val transformer = TransformerFactory.newInstance().newTransformer()
      transformer.transform(source, saxResult)
      adapter.rootElem
    }

    def toPrettyString(jNode: JNode, transformer: Transformer = defaultTransformer): String = {
      val sw = new StringWriter
      transformer.transform(new DOMSource(jNode), new StreamResult(sw))
      sw.toString
    }
  }
}
