package advxml.core.utils

import java.io.StringWriter

import advxml.core.utils.JavaXmlConverters.{JDocument, JNode}
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import javax.xml.transform.{OutputKeys, Transformer, TransformerFactory}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamResult

import scala.xml.{Elem, Node, Text}
import scala.xml.parsing.NoBindingFactoryAdapter

object JavaXmlConverters extends JavaNodeOps with ScalaNodeOps {

  type JDocument = org.w3c.dom.Document
  type JNode = org.w3c.dom.Node
  type JElement = org.w3c.dom.Element
  lazy val documentBuilder: DocumentBuilder = DocumentBuilderFactory
    .newInstance()
    .newDocumentBuilder()
}

private[utils] sealed trait JavaNodeOps {

  implicit class JavaNodeOps(jNode: JNode) {

    def asScala: Node = {
      val source = new DOMSource(jNode)
      val adapter = new NoBindingFactoryAdapter
      val saxResult = new SAXResult(adapter)
      val transformer = TransformerFactory.newInstance().newTransformer()
      transformer.transform(source, saxResult)
      adapter.rootElem
    }

    def toPrettyString: String = {
      val transformer = TransformerFactory.newInstance.newTransformer
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      transformer.setOutputProperty(OutputKeys.METHOD, "xml")
      transformer.setOutputProperty(OutputKeys.INDENT, "yes")
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
      toPrettyString(transformer)
    }

    def toPrettyString(transformer: Transformer): String = {
      val sw = new StringWriter
      transformer.transform(new DOMSource(jNode), new StreamResult(sw))
      sw.toString
    }
  }

  implicit class JavaDocumentOps(jDoc: JDocument) extends JavaNodeOps(jDoc) {
    override def asScala: Elem = super.asScala.asInstanceOf[Elem]
  }
}

private[utils] sealed trait ScalaNodeOps {

  implicit class ScalaNodeOps(node: Node) {

    def asJava(doc: JDocument): JNode =
      node match {
        case Elem(_, label, attributes, _, children @ _*) =>
          val r = doc.createElement(label)
          for (a <- attributes) {
            r.setAttribute(a.key, a.value.text)
          }
          for (c <- children) {
            r.appendChild(c.asJava(doc))
          }
          r
        case Text(text) => doc.createTextNode(text)
        case _          => doc
      }
  }

  implicit class ScalaElemOps(elem: Elem) {

    def asJava: JDocument = {
      val doc = JavaXmlConverters.documentBuilder.newDocument()
      doc.appendChild(elem.asInstanceOf[Node].asJava(doc))
      doc
    }
  }
}
