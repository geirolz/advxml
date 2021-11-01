package advxml.core.utils

import java.io.StringReader

import advxml.core.utils.JavaXmlConverters.{JDocument, JNode}
import advxml.core.utils.JavaXmlConvertersTest.ContractFuncs
import advxml.testUtils.{ContractTests, FunSuiteContract}
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.{Elem, InputSource, Node}

/** Advxml Created by geirolad on 10/07/2019.
  *
  * @author
  *   geirolad
  */
class JavaXmlConvertersTest extends AnyFunSuite with FunSuiteContract {

  JavaXmlConvertersTest
    .Contract(
      // format: off
      f = ContractFuncs(
        asJava          = JavaXmlConverters.FromScala.asJava,
        asJavaWithNode  = JavaXmlConverters.FromScala.asJava,
        jNodeAsScala    = JavaXmlConverters.FromJava.asScala,
        jDocAsScala     = JavaXmlConverters.FromJava.asScala,
        toPrettyString  = JavaXmlConverters.FromJava.toPrettyString(_)
      )
      // format: on
    )
    .runAll()
}

object JavaXmlConvertersTest {

  case class ContractFuncs(
    asJava: Elem => JDocument,
    asJavaWithNode: (Node, JDocument) => JNode,
    jNodeAsScala: JNode => Node,
    jDocAsScala: JDocument => Node,
    toPrettyString: JNode => String
  )

  case class Contract(subDesc: String = "", f: ContractFuncs)
      extends ContractTests("JavaXmlConverters", subDesc) {

    import JavaXmlConverters._
    lazy val xmlStr: String = "<Test T1=\"TEST\"><NestedTest T2=\"NestedTest\"/>TEXT</Test>"
    lazy val xml: Elem      = <Test T1="TEST"><NestedTest T2="NestedTest"/>TEXT</Test>

    lazy val javaDocBuilder: DocumentBuilder = DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()

    lazy val buildJavaDoc: String => JDocument = xmlString =>
      javaDocBuilder.parse(new InputSource(new StringReader(xmlString)))

    test("Convert Java w3c Node to Scala xml Node") {
      val jDocument: JNode = buildJavaDoc(xmlStr)
      val scalaNode        = f.jNodeAsScala(jDocument)

      assert(scalaNode == xml)
    }

    test("Convert Java w3c Document to Scala xml Node") {
      val jDocument: JDocument = buildJavaDoc(xmlStr)
      val scalaNode            = f.jDocAsScala(jDocument)

      assert(scalaNode == xml)
    }

    test("Convert Scala xml Node to Java w3c Node") {
      val jDoc: JNode       = f.asJavaWithNode(xml.asInstanceOf[Node], javaDocBuilder.newDocument())
      val jDocAsStr: String = f.toPrettyString(jDoc)

      assert(jDocAsStr == xmlStr)
    }

    test("Convert Scala xml Elem to Java w3c Document") {
      val jDoc: JDocument   = f.asJava(xml)
      val jDocAsStr: String = f.toPrettyString(jDoc)

      assert(jDocAsStr == xmlStr)
    }
  }
}
