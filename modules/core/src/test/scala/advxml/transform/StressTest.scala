package advxml.transform

import advxml.transform.XmlZoom.root
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.{NodeSeq, XML}

class StressTest extends AnyFunSuite {

  import advxml.data.XmlPredicate.*
  import advxml.transform.XmlModifier.*
  import advxml.implicits.*
  import cats.instances.try_.*
  import cats.syntax.monoid.*

  test("Xml manipulation stress test -Large file: 1MB") {

    val elem = XML.loadFile(getClass.getResource("/transform/stressTest_1mb.xml").getPath)

    val zoomByAttrs1: XmlZoom = root | attrs(
      k"gdp_serv" === 55.20,
      k"government" === "republic",
      k"inflation" === 28.3,
      k"population" === "10002541"
    )

    val zoomByAttrs2: XmlZoom = root | attrs(
      k"capital" === "f0_1533",
      k"car_code" === "H"
    )

    val filterByChild: XmlZoom = root | hasImmediateChild(
      "province",
      attrs(
        k"population" === "422500",
        k"country" === "f0_251",
        k"name" === "Fejer",
        k"capital" === "f0_3117"
      )
    )

    val z: XmlZoom = root / "country" |+| zoomByAttrs1 |+| zoomByAttrs2 |+| filterByChild

    val result: Try[NodeSeq] = elem.transform[Try](
      z ==> SetAttrs(
        k"TEST"  := 1,
        k"TEST2" := 100
      )
    )

    assert(z.run[Try](result.get).get \@ "TEST" == "1")
    assert(z.run[Try](result.get).get \@ "TEST2" == "100")
  }
}
