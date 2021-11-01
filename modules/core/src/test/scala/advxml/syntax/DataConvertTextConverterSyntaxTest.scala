package advxml.syntax

import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Text

/** Advxml Created by geirolad on 03/07/2019.
  *
  * @author
  *   geirolad
  */
class DataConvertTextConverterSyntaxTest extends AnyFunSuite {

  import advxml.syntax.data._
  import advxml.instances.data.convert._
  import cats.instances.option._

  test("String to Text") {

    val value: Option[String] = Some("TEST")
    val res                   = <Test Value={value.mapAs[Text]}/>

    assert(value.contains(res \@ "Value"))
  }

  test("Byte to Text") {
    val value: Option[Byte] = Some(0x001)
    val res                 = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Char to Text") {
    val value: Option[Char] = Some('A')
    val res                 = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Short to Text") {
    val value: Option[Short] = Some(1)
    val res                  = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Int to Text") {
    val value: Option[Int] = Some(100)
    val res                = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Long to Text") {
    val value: Option[Long] = Some(100L)
    val res                 = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Float to Text") {
    val value: Option[Float] = Some(100.55373f)
    val res                  = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("Double to Text") {
    val value: Option[Double] = Some(100.55275373d)
    val res                   = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("BigInt to Text") {
    val value: Option[BigInt] = Some(BigInt(1453472357))
    val res                   = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }

  test("BigDecimal to Text") {
    val value: Option[BigDecimal] = Some(BigDecimal(100.5527537255633))
    val res                       = <Test Value={value.mapAs[Text]}/>

    assert(value.map(_.toString).contains(res \@ "Value"))
  }
}
