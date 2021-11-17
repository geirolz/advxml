package advxml.syntax

import advxml.utils.JavaXmlConverters
import advxml.utils.JavaXmlConverters.{JDocument, JNode}
import advxml.utils.JavaXmlConverters.FromJava.defaultTransformer
import cats.{Applicative, Monad}

import javax.xml.transform.Transformer
import scala.xml.{Elem, Node}

private[advxml] trait AllSyntax extends AllCommonSyntax with AllTransformSyntax with AllDataSyntax

private[syntax] trait AllCommonSyntax extends NestedMapSyntax with JavaScalaConvertersSyntax

//============================== NESTED MAP ==============================
private[syntax] trait NestedMapSyntax {

  import cats.implicits.*

  implicit class ApplicativeDeepMapOps[F[_]: Applicative, G[_]: Applicative, A](fg: F[G[A]]) {
    def nestedMap[B](f: A => B): F[G[B]] = fg.map(_.map(f))
  }

  implicit class ApplicativeDeepFlatMapOps[F[_]: Applicative, G[_]: Monad, A](fg: F[G[A]]) {
    def nestedFlatMap[B](f: A => G[B]): F[G[B]] = fg.map(_.flatMap(f))
  }
}

private[syntax] trait JavaScalaConvertersSyntax {

  // ============================== SCALA => JAVA ==============================
  implicit class JavaToScalaJNodeOps(jNode: JNode) {

    def asScala: Node = JavaXmlConverters.FromJava.asScala(jNode)

    def toPrettyString(transformer: Transformer = defaultTransformer): String =
      JavaXmlConverters.FromJava.toPrettyString(jNode, transformer)
  }

  implicit class JavaToScalaJDocumentOps(jDoc: JDocument) extends JavaToScalaJNodeOps(jDoc) {
    override def asScala: Elem = JavaXmlConverters.FromJava.asScala(jDoc).asInstanceOf[Elem]
  }

  // ============================== SCALA => JAVA ==============================
  implicit class ScalaToJavaNodeOps(node: Node) {
    def asJava(doc: JDocument): JNode = JavaXmlConverters.FromScala.asJava(node, doc)
  }

  implicit class ScalaToJavaElemOps(elem: Elem) {
    def asJava: JDocument = JavaXmlConverters.FromScala.asJava(elem)
  }
}
