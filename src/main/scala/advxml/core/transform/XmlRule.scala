package advxml.core.transform

import advxml.core.transform.actions.{ComposableXmlModifier, FinalXmlModifier, XmlZoom}

/** advxml
  * Created by geirolad on 09/06/2019.
  *
  * @author geirolad
  */
sealed trait XmlRule {
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

  def apply(zoom: XmlZoom, modifiers: List[ComposableXmlModifier]): ComposableXmlRule =
    ComposableXmlRuleImpl(zoom, modifiers)

  def apply(zoom: XmlZoom, modifier: FinalXmlModifier): FinalXmlRule =
    FinalXmlRuleImpl(zoom, modifier)

  private case class ComposableXmlRuleImpl(zoom: XmlZoom, modifiers: List[ComposableXmlModifier])
      extends ComposableXmlRule {

    override def withModifier(modifier: ComposableXmlModifier): ComposableXmlRule =
      copy(modifiers = modifiers :+ modifier)
  }

  private case class FinalXmlRuleImpl(zoom: XmlZoom, modifier: FinalXmlModifier) extends FinalXmlRule
}
