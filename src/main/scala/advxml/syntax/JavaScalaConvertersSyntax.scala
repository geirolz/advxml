package advxml.syntax

import advxml.core.utils.JavaXmlConverters
import advxml.core.utils.JavaXmlConverters.{JDocument, JNode}
import advxml.core.utils.JavaXmlConverters.FromJava.defaultTransformer
import javax.xml.transform.Transformer

import scala.xml.{Elem, Node}

private[syntax] trait JavaScalaConvertersSyntax {

  //*********************************** SCALA => JAVA *************************************
  implicit class JavaToScalaJNodeOps(jNode: JNode) {

    def asScala: Node = JavaXmlConverters.FromJava.asScala(jNode)

    def toPrettyString(transformer: Transformer = defaultTransformer): String =
      JavaXmlConverters.FromJava.toPrettyString(jNode, transformer)
  }

  implicit class JavaToScalaJDocumentOps(jDoc: JDocument) extends JavaToScalaJNodeOps(jDoc) {
    override def asScala: Elem = JavaXmlConverters.FromJava.asScala(jDoc).asInstanceOf[Elem]
  }

  //*********************************** SCALA => JAVA *************************************
  implicit class ScalaToJavaNodeOps(node: Node) {
    def asJava(doc: JDocument): JNode = JavaXmlConverters.FromScala.asJava(node, doc)
  }

  implicit class ScalaToJavaElemOps(elem: Elem) {
    def asJava: JDocument = JavaXmlConverters.FromScala.asJava(elem)
  }
}
