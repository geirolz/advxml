package advxml.core.transform

import advxml.core.transform.actions.{XmlModifier, XmlZoom}
import advxml.core.utils.XmlUtils
import advxml.core.MonadEx
import advxml.instances.transform._
import cats.kernel.Monoid
import cats.syntax.all._

import scala.xml.{Elem, NodeSeq}

object XmlTransformer {

  def transform[F[_]](root: NodeSeq, rules: Seq[XmlRule])(implicit F: MonadEx[F]): F[NodeSeq] =
    rules.foldLeft(F.pure(root))((actDoc, rule) => actDoc.flatMap(doc => transform(rule, doc)))

  private final def transform[F[_]](rule: XmlRule, root: NodeSeq)(implicit F: MonadEx[F]): F[NodeSeq] = {

    def buildRewriteRule(modifier: XmlModifier): (XmlZoom, NodeSeq) => F[NodeSeq] = (zoom, root) => {

      import cats.implicits._

      for {
        target <- zoom[F](root)
        targetNodeSeq = target.nodeSeq
        targetParents = target.parents
        updatedTarget <- modifier[F](targetNodeSeq)
        updatedWholeDocument = {
          targetParents
            .foldRight(XmlPatch.const(targetNodeSeq, updatedTarget))((parent, patch) =>
              XmlPatch(
                parent,
                _.flatMap { case e: Elem =>
                  XmlUtils.flatMapChildren(
                    e,
                    n =>
                      patch.zipWithUpdated
                        .getOrElse(Some(n), Some(n))
                        .getOrElse(NodeSeq.Empty)
                  )
                }
              )
            )
            .updated
        }
      } yield updatedWholeDocument
    }

    (rule match {
      case r: ComposableXmlRule => buildRewriteRule(Monoid.combineAll(r.modifiers))
      case r: FinalXmlRule      => buildRewriteRule(r.modifier)
    })(rule.zoom, root)
  }
}
