package advxml.core.utils

import java.io.StringReader

import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import org.scalatest.FunSuite

import scala.xml.{Elem, InputSource, Node}

/**
  * Advxml
  * Created by geirolad on 10/07/2019.
  *
  * @author geirolad
  */
class JavaXmlConvertersTest extends FunSuite {

  import JavaXmlConverters._

  private val xmlStr: String = "<Test T1=\"TEST\"><NestedTest T2=\"NestedTest\"/>TEXT</Test>"
  private val xml: Elem = <Test T1="TEST"><NestedTest T2="NestedTest"/>TEXT</Test>

  test("Convert Java w3c Node to Scala xml Node") {
    val jDocument: JNode = buildJavaDoc(xmlStr)
    val scalaNode = jDocument.asScala

    assert(scalaNode == xml)
  }

  test("Convert Java w3c document to Scala xml Node") {
    val jDocument: JDocument = buildJavaDoc(xmlStr)
    val scalaNode = jDocument.asScala

    assert(scalaNode == xml)
  }

  test("Convert Scala xml Node to Java w3c Node") {
    val jDoc: JNode = xml.asInstanceOf[Node].asJava(documentBuilder.newDocument())
    val jDocAsStr = jDoc.toPrettyString

    assert(jDocAsStr == xmlStr)
  }

  test("Convert Scala xml Elem to Java w3c Document") {
    val jDoc: JDocument = xml.asJava
    val jDocAsStr = jDoc.toPrettyString

    assert(jDocAsStr == xmlStr)
  }

  lazy val javaDocBuilder: DocumentBuilder = DocumentBuilderFactory
    .newInstance()
    .newDocumentBuilder()

  lazy val buildJavaDoc: String => JDocument = xmlString =>
    javaDocBuilder.parse(new InputSource(new StringReader(xmlString)))
}
