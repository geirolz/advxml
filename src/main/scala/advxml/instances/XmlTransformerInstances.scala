package advxml.instances

import advxml.core.transform.PartialXmlRule

private[instances] trait XmlTransformerInstances extends AllXmlModifierInstances with XmlPredicateInstances {
  lazy val root: PartialXmlRule = PartialXmlRule(identity)
}
