package advxml.instances

import advxml.core.transform.actions.XmlZoom

private[instances] trait XmlTransformerInstances extends AllXmlModifierInstances with XmlPredicateInstances {
  val root: XmlZoom = XmlZoom.root
}
