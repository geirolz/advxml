package advxml.instances

import advxml.core.data.{Converter, ThrowableNel, ValidatedNelEx, Value}
import advxml.core.data.error.AggregatedException
import advxml.core.transform.{XmlContentZoom, XmlContentZoomRunner}
import advxml.implicits.$
import advxml.instances.convert._
import advxml.syntax._
import cats.Id
import cats.data.NonEmptyList
import cats.data.Validated.Valid
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Success, Try}
import scala.xml.{Elem, Node, Text}

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  //============== Id ==============
  implicitly[Converter[Int, Int]].test(1, 1)
  implicitly[Converter[Int, Try[Int]]].test(1, Success(1))

  //============== Node ==============
  Converter[Node, Elem].test(
    <foo/>.asInstanceOf[Node],
    <foo/>
  )

  //============== Throwable ==============
  private val exception = new RuntimeException("")
  Converter[ThrowableNel, Throwable].test(
    ThrowableNel.fromThrowable(exception),
    AggregatedException(NonEmptyList.one(exception))
  )

  //TODO
//  Converter[Throwable, ThrowableNel].test(
//    new RuntimeException(""),
//    ThrowableNel.fromThrowable(new RuntimeException(""))
//  )

}

class ConvertersInstancesTestForValue extends AnyFunSuite with ConvertersAssertsUtils {

  Converter[String, Value].test(
    "TEST",
    Value("TEST")
  )
  Converter[Value, String].test(
    Value("TEST"),
    "TEST"
  )
  Converter[XmlContentZoomRunner, ValidatedNelEx[String]].test(
    XmlContentZoom.attrFromBindedZoom($(<foo bar="1"/>), "bar"),
    Valid("1")
  )
  Converter[XmlContentZoomRunner, Try[String]].test(
    XmlContentZoom.attrFromBindedZoom($(<foo bar="1"/>), "bar"),
    Success("1")
  )

  // format: off
  Converter[Value, Try[BigInt     ]].test(v"1"       , Success(BigInt(1)         ))
  Converter[Value, Try[BigDecimal ]].test(v"1.1234"  , Success(BigDecimal(1.1234)))
  Converter[Value, Try[Byte       ]].test(v"1"       , Success(1.toByte          ))
  Converter[Value, Try[Short      ]].test(v"1"       , Success(1.toShort         ))
  Converter[Value, Try[Char       ]].test(v"A"       , Success('A'               ))
  Converter[Value, Try[Int        ]].test(v"1"       , Success(1                 ))
  Converter[Value, Try[Long       ]].test(v"1"       , Success(1L                ))
  Converter[Value, Try[Float      ]].test(v"1.0"     , Success(1.0f              ))
  Converter[Value, Try[Double     ]].test(v"1.0"     , Success(1.0d              ))

  Converter[BigInt,     Value].test(BigInt(1)         , v"1"     )
  Converter[BigDecimal, Value].test(BigDecimal(1.1234), v"1.1234")
  Converter[Byte,       Value].test(1.toByte          , v"1"     )
  Converter[Short,      Value].test(1.toShort         , v"1"     )
  Converter[Char,       Value].test('A'               , v"A"     )
  Converter[Int,        Value].test(1                 , v"1"     )
  Converter[Long,       Value].test(1L                , v"1"     )
  Converter[Float,      Value].test(1.0f              , v"1.0"   )
  Converter[Double,     Value].test(1.0d              , v"1.0"   )
  // format: on
}
class ConvertersInstancesTestForText extends AnyFunSuite with ConvertersAssertsUtils {

  import advxml.instances.convert._
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
}

private[instances] sealed trait ConvertersAssertsUtils { $this: AnyFunSuite =>

  import scala.reflect.runtime.universe._

  protected implicit class TestConverterIdForSeqOps[I: TypeTag, O: TypeTag](converter: Converter[I, O])
    extends TestConverterForSeqOps[Id, I, O](converter)

  protected implicit class TestConverterForSeqOps[F[_], I: TypeTag, O: TypeTag](converter: Converter[I, F[O]]) {
    def test(in: I, expectedOut: F[O])(implicit foTag: TypeTag[F[O]]): Unit = {
      $this.test(
        s"Converter[${typeOf[I].finalResultType}, ${foTag.tpe.finalResultType}]" +
          s".apply('$in') should be '$expectedOut'"
      ) {
        
        assert(converter(in) == expectedOut)
      }
    }
  }
}
