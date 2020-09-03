package advxml.core.transform

import advxml.core.transform.actions.{XmlModifier, XmlZoom, ZoomedNode}
import advxml.core.transform.exceptions.EmptyTargetException
import advxml.core.validate.MonadEx
import advxml.instances.transform._
import cats.kernel.Monoid
import cats.syntax.all._

import scala.xml.{Elem, NodeSeq}

object XmlTransformer {

  def transform[F[_]](root: NodeSeq, rules: Seq[XmlRule])(implicit F: MonadEx[F]): F[NodeSeq] =
    rules.foldLeft(F.pure(root))((actDoc, rule) => actDoc.flatMap(doc => transform(rule, doc)))

  private final def transform[F[_]](rule: XmlRule, root: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = {

    def buildRewriteRule(modifier: XmlModifier): (XmlZoom, NodeSeq) => F[NodeSeq] = (zoom, root) => {

      import cats.syntax.all._

      for {
        target <- zoom[Option](root) match {
          case Some(target_) => F.pure[ZoomedNode](target_)
          case None          => F.raiseError[ZoomedNode](EmptyTargetException(root, zoom))
        }
        targetNode = target.node
        targetParents = target.parents
        updatedTarget <- modifier[F](targetNode)
        updatedWholeDocument = {
          targetParents
            .foldRight((targetNode, updatedTarget))((parent, originalUpdatedTuple) => {
              val (original, updated) = originalUpdatedTuple
              parent -> parent.flatMap {
                case e: Elem =>
                  val originalIndex = e.child.indexWhere(x => original.xml_sameElements(x))
                  val updatedChild = e.child.updated(originalIndex, updated).flatten
                  e.copy(child = updatedChild)
              }
            })
            ._2
        }
      } yield updatedWholeDocument
    }

    (rule match {
      case r: ComposableXmlRule => buildRewriteRule(Monoid.combineAll(r.modifiers))
      case r: FinalXmlRule      => buildRewriteRule(r.modifier)
    })(rule.zoom, root)
  }
}
