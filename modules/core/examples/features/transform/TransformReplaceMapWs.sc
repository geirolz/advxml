import advxml.implicits._
import cats.instances.try_._

import scala.util.Try
import scala.xml.Elem

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

val result = pets
  .transform[Try](
    $.Cat ==> Replace(oldCatNode => {
      oldCatNode.head.as[Elem].copy(child = oldCatNode \ "Kitty" map (
        k => <c>{k.text}</c>)
      )
    })
  ).get
