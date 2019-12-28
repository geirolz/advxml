package advxml.core.transform

import advxml.core.transform.actions.{ComposableXmlModifier, FinalXmlModifier, XmlModifier}
import advxml.core.transform.actions.XmlZoom.XmlZoom
import advxml.core.validate.MonadEx
import cats.kernel.Monoid

import scala.xml.{Node, NodeSeq}
import scala.xml.transform.RewriteRule

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
sealed trait PartialXmlRule extends ModifierComposableXmlRule {
  val zooms: List[XmlZoom]
  def withModifier(modifier: FinalXmlModifier): FinalXmlRule
}

sealed trait XmlRule {
  val zooms: List[XmlZoom]

  import advxml.instances.transform._
  import cats.syntax.functor._

  final def toRewriteRule[F[_]: MonadEx](root: NodeSeq): F[RewriteRule] =
    (this match {
      case r: ComposableXmlRule => RewriteRuleBuilder(Monoid.combineAll(r.modifiers))
      case r: FinalXmlRule      => RewriteRuleBuilder[F](r.modifier)
    })(zooms.reduce((a, b) => a.andThen(b)), root)

  private object RewriteRuleBuilder {

    def apply[F[_]: MonadEx](modifier: XmlModifier): (XmlZoom, NodeSeq) => F[RewriteRule] =
      (zoom, root) => {
        val target = zoom(root)
        modifier[F](target).map(updated => {
          new RewriteRule {
            var found = false
            override def transform(ns: collection.Seq[Node]): collection.Seq[Node] =
              if (!found && (ns == root || strictEqualsTo(target)(ns))) {
                found = true
                updated
              } else {
                ns
              }
          }
        })
      }
  }
}

sealed trait ComposableXmlRule extends XmlRule with ModifierComposableXmlRule {
  val modifiers: List[ComposableXmlModifier]
}

sealed trait FinalXmlRule extends XmlRule {
  val modifier: FinalXmlModifier
}

private[transform] sealed trait ModifierComposableXmlRule {
  def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule
}

object PartialXmlRule {

  def apply(zoom: XmlZoom, zooms: XmlZoom*): PartialXmlRule =
    PartialXmlRuleImpl((zoom +: zooms).toList)

  private case class PartialXmlRuleImpl(zooms: List[XmlZoom]) extends PartialXmlRule {
    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      ComposableXmlRuleImpl(zooms, List(modifier))

    override def withModifier(modifier: FinalXmlModifier): FinalXmlRule =
      FinalXmlRuleImpl(zooms, modifier)
  }

  private case class ComposableXmlRuleImpl(zooms: List[XmlZoom], modifiers: List[ComposableXmlModifier])
      extends ComposableXmlRule {

    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      copy(modifiers = modifiers :+ modifier)
  }

  private case class FinalXmlRuleImpl(zooms: List[XmlZoom], modifier: FinalXmlModifier) extends FinalXmlRule
}
