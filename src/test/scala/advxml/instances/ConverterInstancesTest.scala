package advxml.instances

import advxml.core.data.{Converter, Value}
import advxml.instances.convert._
import advxml.syntax._
import cats.{Functor, Id}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.Text

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {
  implicitly[Converter[Int, Int]].test(1, 1)
}

class ToStringAndValue_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {
  // format: off
  //  //T => String
  //  Converter[BigInt     , Id[String]].test(BigInt(1)          , "1"     )
  //  Converter[BigDecimal , Id[String]].test(BigDecimal(1.1234) , "1.1234")
  //  Converter[Byte       , Id[String]].test(1.toByte           , "1"     )
  //  Converter[Short      , Id[String]].test(1.toShort          , "1"     )
  //  Converter[Char       , Id[String]].test('A'                , "A"     )
  //  Converter[Int        , Id[String]].test(1                  , "1"     )
  //  Converter[Long       , Id[String]].test(1L                 , "1"     )
  //  Converter[Float      , Id[String]].test(1.0f               , "1.0"   )
  //  Converter[Double     , Id[String]].test(1.0d               , "1.0"   )
  //  
  //T => Value
  Converter[BigInt     , Id[Value]].test(BigInt(1)          , v"1"     )
  Converter[BigDecimal , Id[Value]].test(BigDecimal(1.1234) , v"1.1234")
  Converter[Byte       , Id[Value]].test(1.toByte           , v"1"     )
  Converter[Short      , Id[Value]].test(1.toShort          , v"1"     )
  Converter[Char       , Id[Value]].test('A'                , v"A"     )
  Converter[Int        , Id[Value]].test(1                  , v"1"     )
  Converter[Long       , Id[Value]].test(1L                 , v"1"     )
  Converter[Float      , Id[Value]].test(1.0f               , v"1.0"   )
  Converter[Double     , Id[Value]].test(1.0d               , v"1.0"   )
  // format: on
}

class FromStringAndValue_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  //  //String => T
  //  Converter[String, Try[BigInt     ]].test("1"       , BigInt(1)         )
  //  Converter[String, Try[BigDecimal ]].test("1.1234"  , BigDecimal(1.1234))
  //  Converter[String, Try[Byte       ]].test("1"       , 1.toByte          )
  //  Converter[String, Try[Short      ]].test("1"       , 1.toShort         )
  //  Converter[String, Try[Char       ]].test("A"       , 'A'               )
  //  Converter[String, Try[Int        ]].test("1"       , 1                 )
  //  Converter[String, Try[Long       ]].test("1"       , 1L                )
  //  Converter[String, Try[Float      ]].test("1.0"     , 1.0f              )
  //  Converter[String, Try[Double     ]].test("1.0"     , 1.0d              )

  //Value => T
  Converter[Value, Try[BigInt     ]].test(v"1"       , BigInt(1)         )
  Converter[Value, Try[BigDecimal ]].test(v"1.1234"  , BigDecimal(1.1234))
  Converter[Value, Try[Byte       ]].test(v"1"       , 1.toByte          )
  Converter[Value, Try[Short      ]].test(v"1"       , 1.toShort         )
  Converter[Value, Try[Char       ]].test(v"A"       , 'A'               )
  Converter[Value, Try[Int        ]].test(v"1"       , 1                 )
  Converter[Value, Try[Long       ]].test(v"1"       , 1L                )
  Converter[Value, Try[Float      ]].test(v"1.0"     , 1.0f              )
  Converter[Value, Try[Double     ]].test(v"1.0"     , 1.0d              )
  // format: on
}

class ConvertersInstancesTestForText extends AnyFunSuite with ConvertersAssertsUtils {

  import advxml.instances.convert._
  import cats.instances.try_._

  // format: off
  //Text => T
  Converter[Text, Try[BigInt     ]].test(Text("1")  , BigInt(1)     )
  Converter[Text, Try[BigDecimal ]].test(Text("1")  , BigDecimal(1) )
  Converter[Text, Try[Byte       ]].test(Text("1")  , 1.toByte      )
  Converter[Text, Try[Short      ]].test(Text("1")  , 1.toShort     )
  Converter[Text, Try[Char       ]].test(Text("A")  , 'A'           )
  Converter[Text, Try[Int        ]].test(Text("1")  , 1             )
  Converter[Text, Try[Long       ]].test(Text("1")  , 1L            )
  Converter[Text, Try[Float      ]].test(Text("1.0"), 1.0f          )
  Converter[Text, Try[Double     ]].test(Text("1.0"), 1.0d          )

  //T => Text
  Converter[BigInt     , Id[Text]].test(BigInt(1)    , Text("1")  )
  Converter[BigDecimal , Id[Text]].test(BigDecimal(1), Text("1")  )
  Converter[Byte       , Id[Text]].test(1.toByte     , Text("1")  )
  Converter[Short      , Id[Text]].test(1.toShort    , Text("1")  )
  Converter[Char       , Id[Text]].test('A'          , Text("A")  )
  Converter[Int        , Id[Text]].test(1            , Text("1")  )
  Converter[Long       , Id[Text]].test(1L           , Text("1")  )
  Converter[Float      , Id[Text]].test(1.0f         , Text("1.0"))
  Converter[Double     , Id[Text]].test(1.0d         , Text("1.0"))
}

private[instances] sealed trait ConvertersAssertsUtils { $this: AnyFunSuite =>

  import scala.reflect.runtime.universe._

  protected implicit class TestConverterIdForSeqOps[I: TypeTag, O: TypeTag](converter: Converter[I, O])
    extends TestConverterForSeqOps[Id, I, O](converter)

  protected implicit class TestConverterForSeqOps[F[_]: Functor, I: TypeTag, O: TypeTag](converter: Converter[I, F[O]]) {
    def test(in: I, expectedOut: O)(implicit foTag: TypeTag[F[O]]): Unit = {
      $this.test(
        s"Converter[${typeOf[I].finalResultType}, ${foTag.tpe.finalResultType}]" +
          s".apply('$in') should be '$expectedOut'"
      ) {
        Functor[F].map(converter(in))(v => assert(v == expectedOut))
      }
    }
  }
}
