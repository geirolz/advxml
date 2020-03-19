import advxml.implicits._
import cats.instances.try_._

import scala.xml.{Elem, NodeSeq}

val pets =
  <Pet>
    <Cat a="TEST">
      <Kitty>small</Kitty>
      <Kitty>big</Kitty>
      <Kitty>large</Kitty>
    </Cat>
    <Dog>
      <Kitty>large</Kitty>
    </Dog>
  </Pet>

val result: NodeSeq = pets
  .transform(
    (> \ "Cat") ==> Replace(oldCatNode => {
      oldCatNode.head.asPure[Elem].copy(child = oldCatNode \ "Kitty" map (k => <c>
        {k.text}
      </c>))
    })
  )
  .get

