package advxml

import advxml.data.AllConverterInstances
import advxml.data.error.AggregatedExceptionInstances
import advxml.transform.{XmlModifierInstances, XmlZoomInstances}

private[advxml] trait AllInstances
    extends XmlModifierInstances
    with XmlZoomInstances
    with AggregatedExceptionInstances
    with AllConverterInstances
