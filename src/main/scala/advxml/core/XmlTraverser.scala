package advxml.core

import scala.annotation.implicitNotFound
import scala.language.dynamics
import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 28/06/2019.
  * @author geirolad
  */
object XmlTraverser {

  def apply[F[_]: XmlTraverser]: XmlTraverser[F] = implicitly[XmlTraverser[F]]

  @implicitNotFound(
    "Missing an implicit instance of XmlTraverser for ${F}. Please try to import advxml.instances.traverse._"
  )
  trait XmlTraverser[F[_]] {

    def immediateChildren(target: NodeSeq, q: String): F[NodeSeq]

    def children(ns: NodeSeq, q: String): F[NodeSeq]

    def attr(ns: NodeSeq, q: String): F[String]

    def text(ns: NodeSeq): F[String]

    def trimmedText(ns: NodeSeq): F[String]

    def childTraverser: TraverserK[NodeSeq, NodeSeq, F]
  }
  @implicitNotFound(
    "Missing an implicit instance of XmlMandatoryTraverser for ${F}. Please try to import advxml.instances.traverse._"
  )
  trait XmlMandatoryTraverser[F[_]] extends XmlTraverser[F]
  object XmlMandatoryTraverser {
    def apply[F[_]: XmlMandatoryTraverser]: XmlMandatoryTraverser[F] = implicitly[XmlMandatoryTraverser[F]]
  }
  @implicitNotFound(
    "Missing an implicit instance of XmlOptionalTraverser for ${F}. Please try to import advxml.instances.traverse._"
  )
  trait XmlOptionalTraverser[F[_]] extends XmlTraverser[F]
  object XmlOptionalTraverser {
    def apply[F[_]: XmlOptionalTraverser]: XmlOptionalTraverser[F] = implicitly[XmlOptionalTraverser[F]]
  }

  sealed trait XmlDynamicTraverser[F[_], T <: XmlDynamicTraverser[F, T]] extends Dynamic {

    def get: F[NodeSeq]

    def selectDynamic(q: String): T

    def applyDynamic(q: String)(idx: Int): T
  }
  trait XmlImmediateDynamicTraverser[F[_]] extends XmlDynamicTraverser[F, XmlImmediateDynamicTraverser[F]]
  trait XmlDeepDynamicTraverser[F[_]] extends XmlDynamicTraverser[F, XmlDeepDynamicTraverser[F]]

  object exceptions {

    abstract class XmlMissingException(val message: String) extends RuntimeException(message) {
      val target: NodeSeq
    }

    case class XmlMissingNodeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for node: $q")

    case class XmlMissingAttributeException(q: String, target: NodeSeq)
        extends XmlMissingException(s"Missing match for attribute: $q")

    case class XmlMissingTextException(target: NodeSeq) extends XmlMissingException(s"Missing text, content is empty")
  }
}
