package advxml.instances

import advxml.core.data.{
  As,
  Converter,
  SimpleValue,
  ThrowableNel,
  ValidatedNelThrow,
  ValidatedValue
}
import advxml.core.data.error.AggregatedException
import advxml.core.transform.{XmlContentZoom, XmlContentZoomRunner}
import advxml.implicits.$
import advxml.instances.data.convert._
import advxml.syntax.data._
import cats.data.NonEmptyList
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.{Elem, Node, Text}

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.option._

  // ============== Id ==============
  implicitly[Converter[Int, Int]].test(1, 1)
  implicitly[Converter[Int, Try[Int]]].test(1, Success(1))

  // ============== FlatMapAs ==============
  Converter[Option[SimpleValue], Option[Int]].test(Some(v"1"), Some(1))

  // ============== AndThenAs ==============
  Converter[ValidatedNelThrow[SimpleValue], ValidatedNelThrow[Int]].test(Valid(v"1"), Valid(1))

  // ============== Node ==============
  Converter[Node, Elem].test(
    <foo/>.asInstanceOf[Node],
    <foo/>
  )

  // ============== Throwable ==============
  private val exception = new RuntimeException("")
  Converter[ThrowableNel, Throwable].test(
    ThrowableNel.fromThrowable(exception),
    AggregatedException(NonEmptyList.one(exception))
  )
}

class ConvertersInstancesTestForSimpleValue extends AnyFunSuite with ConvertersAssertsUtils {

  Converter[String, SimpleValue].test(
    "TEST",
    SimpleValue("TEST")
  )
  Converter[SimpleValue, String].test(
    SimpleValue("TEST"),
    "TEST"
  )
  Converter[XmlContentZoomRunner, ValidatedNelThrow[String]].test(
    XmlContentZoom.attrFromBindedZoom($(<foo bar="1"/>), "bar"),
    Valid("1")
  )
  Converter[XmlContentZoomRunner, Try[String]].test(
    XmlContentZoom.attrFromBindedZoom($(<foo bar="1"/>), "bar"),
    Success("1")
  )

  // format: off
  Converter[SimpleValue, Try[BigInt     ]].test(v"1"       , Success(BigInt(1)         ))
  Converter[SimpleValue, Try[BigDecimal ]].test(v"1.1234"  , Success(BigDecimal(1.1234)))
  Converter[SimpleValue, Try[Byte       ]].test(v"1"       , Success(1.toByte          ))
  Converter[SimpleValue, Try[Short      ]].test(v"1"       , Success(1.toShort         ))
  Converter[SimpleValue, Try[Char       ]].test(v"A"       , Success('A'               ))
  Converter[SimpleValue, Try[Int        ]].test(v"1"       , Success(1                 ))
  Converter[SimpleValue, Try[Long       ]].test(v"1"       , Success(1L                ))
  Converter[SimpleValue, Try[Float      ]].test(v"1.0"     , Success(1.0f              ))
  Converter[SimpleValue, Try[Double     ]].test(v"1.0"     , Success(1.0d              ))

  Converter[BigInt,     SimpleValue].test(BigInt(1)         , v"1"     )
  Converter[BigDecimal, SimpleValue].test(BigDecimal(1.1234), v"1.1234")
  Converter[Byte,       SimpleValue].test(1.toByte          , v"1"     )
  Converter[Short,      SimpleValue].test(1.toShort         , v"1"     )
  Converter[Char,       SimpleValue].test('A'               , v"A"     )
  Converter[Int,        SimpleValue].test(1                 , v"1"     )
  Converter[Long,       SimpleValue].test(1L                , v"1"     )
  Converter[Float,      SimpleValue].test(1.0f              , v"1.0"   )
  Converter[Double,     SimpleValue].test(1.0d              , v"1.0"   )
  // format: on
}

class ConvertersInstancesTestForText extends AnyFunSuite with ConvertersAssertsUtils {

  import advxml.instances.data.convert._
  import cats.instances.try_._

  // format: off
  //Text => T
  Converter[Text, Try[BigInt     ]].test(Text("1")  , Success(BigInt(1)     ))
  Converter[Text, Try[BigDecimal ]].test(Text("1")  , Success(BigDecimal(1) ))
  Converter[Text, Try[Byte       ]].test(Text("1")  , Success(1.toByte      ))
  Converter[Text, Try[Short      ]].test(Text("1")  , Success(1.toShort     ))
  Converter[Text, Try[Char       ]].test(Text("A")  , Success('A'           ))
  Converter[Text, Try[Int        ]].test(Text("1")  , Success(1             ))
  Converter[Text, Try[Long       ]].test(Text("1")  , Success(1L            ))
  Converter[Text, Try[Float      ]].test(Text("1.0"), Success(1.0f          ))
  Converter[Text, Try[Double     ]].test(Text("1.0"), Success(1.0d          ))

  //T => Text
  Converter[BigInt     , Text].test(BigInt(1)    , Text("1")  )
  Converter[BigDecimal , Text].test(BigDecimal(1), Text("1")  )
  Converter[Byte       , Text].test(1.toByte     , Text("1")  )
  Converter[Short      , Text].test(1.toShort    , Text("1")  )
  Converter[Char       , Text].test('A'          , Text("A")  )
  Converter[Int        , Text].test(1            , Text("1")  )
  Converter[Long       , Text].test(1L           , Text("1")  )
  Converter[Float      , Text].test(1.0f         , Text("1.0"))
  Converter[Double     , Text].test(1.0d         , Text("1.0"))

  case class CustomType(v: Int)
  implicit val customTypeAsValidatedValueConverter: CustomType As ValidatedValue = 
    Converter.of(ct => SimpleValue(ct.v.toString).nonEmpty)
    
  Converter[CustomType, Try[Text]].test(CustomType(1), Success(Text("1")))
}
