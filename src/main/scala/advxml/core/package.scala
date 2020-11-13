package advxml

import scala.xml.NodeSeq

package object core {
  type Predicate[-A] = A => Boolean
  type XmlPredicate = Predicate[NodeSeq]
}
