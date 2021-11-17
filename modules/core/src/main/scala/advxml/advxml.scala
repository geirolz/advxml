import advxml.data.ThrowableNel
import cats.ApplicativeError

import scala.xml.NodeSeq

package object advxml {

  type Xml = NodeSeq
  val Xml = NodeSeq

  type ApplicativeNelThrow[F[_]] = ApplicativeError[F, ThrowableNel]
  type ApplicativeEu[F[_]]       = ApplicativeError[F, Unit]
}
