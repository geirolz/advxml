package advxml.instances

import advxml.core.transform.actions.XmlZoom

private[instances] trait XmlTransformerInstances extends AllXmlModifierInstances with XmlPredicateInstances {
  lazy val root: XmlZoom = XmlZoom.root
  lazy val > : XmlZoom = XmlZoom.root
}
