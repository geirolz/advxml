package advxml.core.transform

import advxml.core.utils.XmlUtils
import cats.{MonadThrow, Monoid}
import cats.syntax.all._

import scala.xml.{Elem, NodeSeq}

trait AbstractRule
object AbstractRule {
  case class And(a: AbstractRule, b: AbstractRule) extends AbstractRule
  case class OrElse(a: AbstractRule, b: AbstractRule) extends AbstractRule
  case class Optional(a: AbstractRule) extends AbstractRule

  def transform[F[_]](root: NodeSeq, rule: AbstractRule, rules: AbstractRule*)(implicit F: MonadThrow[F]): F[NodeSeq] =
    transform(root, List(rule) ++ rules)

  def transform[F[_]](root: NodeSeq, rules: List[AbstractRule])(implicit F: MonadThrow[F]): F[NodeSeq] =
    rules.foldLeft(F.pure(root))((actDoc, rule) => actDoc.flatMap(doc => transform(doc, rule)))

  def transform[F[_]](doc: NodeSeq, rule: AbstractRule)(implicit F: MonadThrow[F]): F[NodeSeq] =
    rule match {
      case OrElse(a, b)  => transform[F](doc, a).handleErrorWith(_ => transform(doc, b))
      case And(a, b)     => transform[F](doc, a).flatMap(transform(_, b))
      case Optional(a)   => transform[F](doc, a).handleErrorWith(_ => F.pure(doc))
      case rule: XmlRule => XmlRule.transform(doc, rule)
    }
}

//-------------------- SINGLE XML RULE --------------------
sealed trait XmlRule extends AbstractRule {
  val zoom: XmlZoom
}
sealed trait ComposableXmlRule extends XmlRule {
  val modifiers: List[ComposableXmlModifier]
  def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule
}
sealed trait FinalXmlRule extends XmlRule {
  val modifier: FinalXmlModifier
}

object XmlRule {

  import advxml.instances.transform.composableXmlModifierMonoidInstance
  import cats.syntax.all._

  private[transform] def transform[F[_]](root: NodeSeq, rule: XmlRule)(implicit F: MonadThrow[F]): F[NodeSeq] = {
    val modifier = rule match {
      case r: ComposableXmlRule => Monoid.combineAll(r.modifiers)
      case r: FinalXmlRule      => r.modifier
    }
    buildRewriteRule(root, rule.zoom, modifier)
  }

  private def buildRewriteRule[F[_]](root: NodeSeq, zoom: XmlZoom, modifier: XmlModifier)(implicit
    F: MonadThrow[F]
  ): F[NodeSeq] = {
    for {
      target <- zoom.detailed[F](root)
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

  //============================== BUILD ==============================
  def apply(
    zoom: XmlZoom,
    modifier1: ComposableXmlModifier,
    modifiers: ComposableXmlModifier*
  ): ComposableXmlRule =
    Impls.Composable(zoom, (modifier1 +: modifiers).toList)

  def apply(zoom: XmlZoom, modifiers: List[ComposableXmlModifier]): ComposableXmlRule =
    Impls.Composable(zoom, modifiers)

  def apply(zoom: XmlZoom, modifier: FinalXmlModifier): FinalXmlRule =
    Impls.Final(zoom, modifier)

  //============================== IMPLS ==============================
  private object Impls {

    case class Composable(zoom: XmlZoom, modifiers: List[ComposableXmlModifier]) extends ComposableXmlRule {

      override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
        copy(modifiers = modifiers :+ modifier)
    }

    case class Final(zoom: XmlZoom, modifier: FinalXmlModifier) extends FinalXmlRule

  }
}
