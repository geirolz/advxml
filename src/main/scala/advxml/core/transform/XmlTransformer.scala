package advxml.core.transform

import advxml.core.transform.actions.XmlModifier
import advxml.core.transform.actions.XmlZoom.XmlZoom
import advxml.core.validate.MonadEx
import advxml.core.XmlNormalizer
import advxml.core.utils.internals.MutableSingleUse
import advxml.instances.transform._
import cats.instances.list._
import cats.kernel.Monoid
import cats.syntax.all._

import scala.xml.{Node, NodeSeq}
import scala.xml.transform.{BasicTransformer, RewriteRule, RuleTransformer}

object XmlTransformer {

  def transform[F[_]: MonadEx](root: NodeSeq, rules: Seq[XmlRule]): F[NodeSeq] =
    transform(new RuleTransformer(_: _*))(root, rules)

  def transform[F[_]: MonadEx](
    f: Seq[RewriteRule] => BasicTransformer
  )(root: NodeSeq, rules: Seq[XmlRule]): F[NodeSeq] =
    rules
      .map(toRewriteRule[F](_, root))
      .toList
      .sequence
      .map(f(_).transform(root))

  private final def toRewriteRule[F[_]: MonadEx](rule: XmlRule, root: NodeSeq): F[RewriteRule] = {

    def buildRewriteRule(modifier: XmlModifier): (XmlZoom, NodeSeq) => F[RewriteRule] =
      (zoom, root) => {
        val target = zoom(root)
        modifier[F](target)
          .map(MutableSingleUse(_))
          .map(updated => {
            new RewriteRule {
              override def transform(ns: collection.Seq[Node]): collection.Seq[Node] =
                if (XmlNormalizer.normalizedEquals(target, ns))
                  updated.getOrElse(ns)
                else
                  ns
            }
          })
      }

    (rule match {
      case r: ComposableXmlRule => buildRewriteRule(Monoid.combineAll(r.modifiers))
      case r: FinalXmlRule      => buildRewriteRule(r.modifier)
    })(rule.zooms.reduce((a, b) => a.andThen(b)), root)
  }
}
