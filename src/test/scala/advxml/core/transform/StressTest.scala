package advxml.core.transform

import advxml.core.transform.actions.XmlZoom
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{NodeSeq, XML}

class StressTest extends AnyFunSuite {

  import advxml.instances.convert._
  import advxml.instances.transform._
  import advxml.syntax.transform._
  import cats.instances.try_._
  import cats.instances.option._

  test("Xml manipulation stress test -Large file: 1MB") {

    val elem = XML.loadFile(getClass.getResource("/transform/stressTest_1mb.xml").getPath)

    val zoomByAttrs1: XmlZoom = root filter attrs(
        "gdp_serv"   -> (_ == "55.2"),
        "government" -> (_ == "republic"),
        "inflation"  -> (_ == "28.3"),
        "population" -> (_ == "10002541")
      )

    val zoomByAttrs2: XmlZoom = root filter attrs(
        "capital"  -> (_ == "f0_1533"),
        "car_code" -> (_ == "H")
      )

    val filterByChild: XmlZoom = root filter hasImmediateChild(
        "province",
        attrs(
          "population" -> (_ == "422500"),
          "country"    -> (_ == "f0_251"),
          "name"       -> (_ == "Fejer"),
          "capital"    -> (_ == "f0_3117")
        )
      )

    val z: XmlZoom = root \ "country" \+ zoomByAttrs1 \+ zoomByAttrs2 \+ filterByChild

    val result: Try[NodeSeq] = elem.transform[Try](z ==> SetAttrs("TEST" := "1", "TEST2" := "100"))

    assert(result.isSuccess)
    assert(z[Option](result.get).get.node \@ "TEST" == "1")
    assert(z[Option](result.get).get.node \@ "TEST2" == "100")
  }
}
