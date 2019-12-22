package advxml.core.utils

import java.io.StringReader

import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import org.scalatest.FunSuite

import scala.xml.{InputSource, Node}

/**
  * Advxml
  * Created by geirolad on 10/07/2019.
  *
  * @author geirolad
  */
class JavaXmlConvertersTest extends FunSuite {

  import JavaXmlConverters._

  test("Convert Java w3c Node to Scala xml Node") {
    val jDocument: JNode = buildJavaDoc("<Test></Test>")
    val scalaNode = jDocument.asScala

    assert(scalaNode == <Test></Test>)
  }

  test("Convert Java w3c document to Scala xml Node") {
    val jDocument: JDocument = buildJavaDoc("<Test></Test>")
    val scalaNode = jDocument.asScala

    assert(scalaNode == <Test></Test>)
  }

  test("Convert Scala xml Node to Java w3c Node") {
    val scalaNode: Node = <Test/>
    val jDoc: JNode = scalaNode.asJava(documentBuilder.newDocument())
    val jDocAsStr = jDoc.toPrettyString

    assert(jDocAsStr.trim == "<Test/>")
  }

  test("Convert Scala xml Elem to Java w3c Document") {
    val scalaNode = <Test/>
    val jDoc: JDocument = scalaNode.asJava
    val jDocAsStr = jDoc.toPrettyString

    assert(jDocAsStr.trim == "<Test/>")
  }

  lazy val javaDocBuilder: DocumentBuilder = DocumentBuilderFactory
    .newInstance()
    .newDocumentBuilder()

  lazy val buildJavaDoc: String => JDocument = xmlString =>
    javaDocBuilder.parse(new InputSource(new StringReader(xmlString)))
}
