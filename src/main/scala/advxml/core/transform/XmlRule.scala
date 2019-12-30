package advxml.core.transform

import advxml.core.transform.actions.{ComposableXmlModifier, FinalXmlModifier}
import advxml.core.transform.actions.XmlZoom.XmlZoom

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
