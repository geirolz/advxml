import advxml.core.transform.XmlZoom.ImmediateDown
import advxml.core.transform.XmlPatch

import scala.xml.{Elem, Group, SpecialNode}

object Test {

  import scala.xml.NodeSeq
  import advxml.implicits._
  import cats.instances.try_._

  import scala.util.Try
  import advxml.core.transform.{XmlZoom, ZoomResult}

  val xml =
    <n1>
      <n2>
        <n3>
          <n4/>
          //1
          <n4/>
          //2
          <n4/>
          //3
        </n3>
      </n2>
    </n1>

  val zoom: XmlZoom = root.n2.n3.n4.head()
  val actions: Seq[XmlZoom.ZoomAction] = zoom.actions
  val zoomResult: ZoomResult = zoom[Try](xml).get
  val target = zoomResult.nodeSeq
  val updated: NodeSeq = <n4 t="new"/>
  val lastDown = actions.reverse.find {
    case ImmediateDown(_) => true
    case _ => false
  }


  zoomResult.parents.reverse.foldLeft(
    XmlPatch.const(target, updated)
  )((patch, parent) => {

    parent.flatMap {
      case elem: Elem =>
      case _ => _
    }

  })
}

Test.lastDown






