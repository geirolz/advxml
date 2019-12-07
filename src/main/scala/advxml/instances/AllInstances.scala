package advxml.instances

import advxml.instances.transform.XmlTransformerInstances

private[advxml] trait AllInstances extends XmlTransformerInstances with ConvertersInstances with ValidationInstance
