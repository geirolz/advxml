package com.dgz.advxml.transform

import com.dgz.advxml.transform.actions._

import scala.xml.transform.RewriteRule
import scala.xml.{Node, NodeSeq}

/**
  * advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */

sealed trait PartialXmlRule extends ModifierComposableXmlRule{
  val zoom: XmlZoom
  def withModifier(modifier: FinalXmlModifier): FinalXmlRule
}

sealed trait XmlRule{
  val zoom: XmlZoom
  val modifier: XmlModifier

  final def toRewriteRule[F[_] : MonadEx](root: NodeSeq): F[RewriteRule] = {

    import cats.implicits._

    val target = zoom(root)

    modifier[F](target).map(updated => {
      new RewriteRule {
        override def transform(ns: Seq[Node]): Seq[Node] =
          if(ns == root || Filters.equalsTo(target)(ns)) updated else ns
      }
    })
  }
}

sealed trait ComposableXmlRule extends XmlRule with ModifierComposableXmlRule{
  val modifier: ComposableXmlModifier
}

sealed trait FinalXmlRule extends XmlRule {
  val modifier: FinalXmlModifier
}

private [transform] sealed trait ModifierComposableXmlRule{
  def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule
}



object PartialXmlRule{

  def apply(zoom: XmlZoom): PartialXmlRule = PartialXmlRuleImpl(zoom)


  private [transform] case class PartialXmlRuleImpl(zoom: XmlZoom) extends PartialXmlRule{
    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      ComposableXmlRuleImpl(zoom, modifier)

    override def withModifier(modifier: FinalXmlModifier): FinalXmlRule =
      FinalXmlRuleImpl(zoom, modifier)
  }

  private [transform] case class ComposableXmlRuleImpl(zoom: XmlZoom, modifier: ComposableXmlModifier) extends ComposableXmlRule {

    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      copy(modifier = this.modifier.andThen(modifier))
  }

  private [transform] case class FinalXmlRuleImpl(zoom: XmlZoom, modifier: FinalXmlModifier) extends FinalXmlRule
}