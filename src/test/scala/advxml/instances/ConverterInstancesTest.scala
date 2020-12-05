package advxml.instances

import advxml.core.data.Converter
import cats.{Id, Monad}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import advxml.instances.convert._

import scala.xml.Text

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  implicitly[Converter[Try, Int, Int]].test(1, 1)
  implicitly[Converter[Id,  Int, Int]].test(1, 1)
  // format: on
}

class ToString_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {
  // format: off
  Converter[Id, BigInt     , String].test(BigInt(1)          , "1"     )
  Converter[Id, BigDecimal , String].test(BigDecimal(1.1234) , "1.1234")
  Converter[Id, Byte       , String].test(1.toByte           , "1"     )
  Converter[Id, Short      , String].test(1.toShort          , "1"     )
  Converter[Id, Char       , String].test('A'                , "A"     )
  Converter[Id, Int        , String].test(1                  , "1"     )
  Converter[Id, Long       , String].test(1L                 , "1"     )
  Converter[Id, Float      , String].test(1.0f               , "1.0"   )
  Converter[Id, Double     , String].test(1.0d               , "1.0"   )
  // format: on
}

class FromString_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  //String => T
  Converter[Id, String, BigInt     ].test("1"       , BigInt(1)         )
  Converter[Id, String, BigDecimal ].test("1.1234"  , BigDecimal(1.1234))
  Converter[Id, String, Byte       ].test("1"       , 1.toByte          )
  Converter[Id, String, Short      ].test("1"       , 1.toShort         )
  Converter[Id, String, Char       ].test("A"       , 'A'               )
  Converter[Id, String, Int        ].test("1"       , 1                 )
  Converter[Id, String, Long       ].test("1"       , 1L                )
  Converter[Id, String, Float      ].test("1.0"     , 1.0f              )
  Converter[Id, String, Double     ].test("1.0"     , 1.0d              )
  
  //String => T
  Converter[Try, String, BigInt     ].test("1"       , BigInt(1)         )
  Converter[Try, String, BigDecimal ].test("1.1234"  , BigDecimal(1.1234))
  Converter[Try, String, Byte       ].test("1"       , 1.toByte          )
  Converter[Try, String, Short      ].test("1"       , 1.toShort         )
  Converter[Try, String, Char       ].test("A"       , 'A'               )
  Converter[Try, String, Int        ].test("1"       , 1                 )
  Converter[Try, String, Long       ].test("1"       , 1L                )
  Converter[Try, String, Float      ].test("1.0"     , 1.0f              )
  Converter[Try, String, Double     ].test("1.0"     , 1.0d              )
  // format: on
}

class ConvertersInstancesTestForText extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._
  import advxml.instances.convert._

  // format: off
  //Text => T
  //String => BigDecimal 
  Converter[Try, Text, BigInt     ].test(Text("1")  , BigInt(1)     )
  Converter[Try, Text, BigDecimal ].test(Text("1")  , BigDecimal(1) )
  Converter[Try, Text, Byte       ].test(Text("1")  , 1.toByte      )
  Converter[Try, Text, Short      ].test(Text("1")  , 1.toShort     )
  Converter[Try, Text, Char       ].test(Text("A")  , 'A'           )
  Converter[Try, Text, Int        ].test(Text("1")  , 1             )
  Converter[Try, Text, Long       ].test(Text("1")  , 1L            )
  Converter[Try, Text, Float      ].test(Text("1.0"), 1.0f          )
  Converter[Try, Text, Double     ].test(Text("1.0"), 1.0d          )

  //T => Text
  Converter[Try, BigInt     , Text].test(BigInt(1)    , Text("1")  )
  Converter[Try, BigDecimal , Text].test(BigDecimal(1), Text("1")  )
  Converter[Try, Byte       , Text].test(1.toByte     , Text("1")  )
  Converter[Try, Short      , Text].test(1.toShort    , Text("1")  )
  Converter[Try, Char       , Text].test('A'          , Text("A")  )
  Converter[Try, Int        , Text].test(1            , Text("1")  )
  Converter[Try, Long       , Text].test(1L           , Text("1")  )
  Converter[Try, Float      , Text].test(1.0f         , Text("1.0"))
  Converter[Try, Double     , Text].test(1.0d         , Text("1.0"))
}

private[instances] sealed trait ConvertersAssertsUtils { $this: AnyFunSuite =>

  import scala.reflect.runtime.universe._

  protected implicit class TestConverterForSeqOps[F[_]: Monad, I: TypeTag, O: TypeTag](converter: Converter[F, I, O]) {
    def test(in: I, expectedOut: O)(implicit foTag: TypeTag[F[O]]): Unit = {
      $this.test(
        s"Converter[${typeOf[I].finalResultType}, ${foTag.tpe.finalResultType}]" +
        s".apply('$in') should be '$expectedOut'"
      ) {
        Monad[F].map(converter(in))(v => assert(v == expectedOut))
      }
    }
  }
}
