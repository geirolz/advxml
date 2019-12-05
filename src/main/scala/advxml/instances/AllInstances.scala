package advxml.instances

import advxml.instances.transformation.XmlTransformerInstances

private[advxml] trait AllInstances extends XmlTransformerInstances with ConvertersInstances with ValidationInstance
