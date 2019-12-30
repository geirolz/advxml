package advxml.instances

import advxml.core.transform.PartialXmlRule

trait XmlTransformerInstances extends AllXmlModifierInstances with XmlPredicateInstances {
  lazy val root: PartialXmlRule = PartialXmlRule(identity)
}
