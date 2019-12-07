package advxml.core.transform

import advxml.core.transform.actions.XmlZoom
import advxml.core.transform.actions.XmlZoom.XmlZoom
import org.scalatest.FunSuite

import scala.util.Try
import scala.xml.XML

class StressTest extends FunSuite {

  import advxml.instances.convert._
  import advxml.instances.transform._
  import advxml.syntax.transform._
  import cats.instances.try_._

  test("Xml manipulation stress test -Large file: 1MB") {

    val elem = XML.loadFile(getClass.getResource("/transform/stressTest_1mb.xml").getPath)

    val zoomByAttrs1: XmlZoom = _ filter attrs(
        "gdp_serv"   -> (_ == "55.2"),
        "government" -> (_ == "republic"),
        "inflation"  -> (_ == "28.3"),
        "population" -> (_ == "10002541")
      )

    val zoomByAttrs2: XmlZoom = _ filter attrs(
        "capital"  -> (_ == "f0_1533"),
        "car_code" -> (_ == "H")
      )

    val filterByChild: XmlZoom = _ filter hasImmediateChild(
        "province",
        attrs(
          "population" -> (_ == "422500"),
          "country"    -> (_ == "f0_251"),
          "name"       -> (_ == "Fejer"),
          "capital"    -> (_ == "f0_3117")
        )
      )

    val z: XmlZoom = XmlZoom(_ \ "country") \ zoomByAttrs1 \ zoomByAttrs2 \ filterByChild

    val result = elem.transform[Try]($(z) ==> SetAttrs("TEST" := "1", "TEST2" := "100"))

    assert(result.isSuccess)
    assert(z(result.get) \@ "TEST" == "1")
    assert(z(result.get) \@ "TEST2" == "100")
  }
}
