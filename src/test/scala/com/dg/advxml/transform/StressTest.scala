package com.dg.advxml.transform

import org.scalatest.FeatureSpec

import scala.xml.XML

class StressTest extends FeatureSpec  {

  import com.dg.advxml.AdvXml._

  feature("Xml manipulation stress test") {
    scenario("Large file: 1MB") {

      val elem = XML.loadFile(getClass.getResource("/transform/stressTest_1mb.xml").getPath)

      val zoomByAttrs1: XmlZoom = _ filter attrs(
        "gdp_serv" -> "55.2",
        "government" -> "republic",
        "inflation" -> "28.3",
        "population" -> "10002541")

      val zoomByAttrs2: XmlZoom = _ filter attrs(
        "capital" -> "f0_1533",
        "car_code" -> "H")

      val filterByChild: XmlZoom = _ filter hasImmediateChild("province",
        attrs(
          "population" -> "422500",
          "country" -> "f0_251",
          "name" -> "Fejer",
          "capital" -> "f0_3117"
        )
      )

      val seq = elem.transform(
        $(zoom(_ \ "country") \ zoomByAttrs1 \ zoomByAttrs2 \ filterByChild)
          ==> setAttrs("TEST" -> "1", "TEST2" -> "100")
      )
      Console.println(seq)
    }
  }
}
