package advxml.instances

import advxml.core.Converter
import cats.{Id, Monad}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try
import scala.xml.Text

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  implicitly[Converter[Try, Int, Int]].test(1, 1)
  implicitly[Converter[Id,  Int, Int]].test(1, 1)
  // format: on
}

class ToText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {
  // format: off
  id_string_to_text      test("TEST"             , Text("TEST")  )
  id_bigInt_to_text      test(BigInt(1)          , Text("1")     )
  id_bigDecimal_to_text  test(BigDecimal(1.1234) , Text("1.1234"))
  id_byte_to_text        test(1.toByte           , Text("1")     )
  id_short_to_text       test(1.toShort          , Text("1")     )
  id_char_to_text        test('A'                , Text("A")     )
  id_int_to_text         test(1                  , Text("1")     )
  id_long_to_text        test(1L                 , Text("1")     )
  id_float_to_text       test(1.0f               , Text("1.0")   )
  id_double_to_text      test(1.0d               , Text("1.0")   )
  // format: on
}

class FromText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  id_text_to_string      test(Text("TEST")       , "TEST"            )
  id_text_to_bigInt      test(Text("1")          , BigInt(1)         )
  id_text_to_bigDecimal  test(Text("1.1234")     , BigDecimal(1.1234))
  id_text_to_byte        test(Text("1")          , 1.toByte          )
  id_text_to_short       test(Text("1")          , 1.toShort         )
  id_text_to_char        test(Text("A")          , 'A'               )
  id_text_to_int         test(Text("1")          , 1                 )
  id_text_to_long        test(Text("1")          , 1L                )
  id_text_to_float       test(Text("1.0")        , 1.0f              )
  id_text_to_double      test(Text("1.0")        , 1.0d              )
  
  monad_text_to_string      test(Text("TEST")       , "TEST"            )
  monad_text_to_bigInt      test(Text("1")          , BigInt(1)         )
  monad_text_to_bigDecimal  test(Text("1.1234")     , BigDecimal(1.1234))
  monad_text_to_byte        test(Text("1")          , 1.toByte          )
  monad_text_to_short       test(Text("1")          , 1.toShort         )
  monad_text_to_char        test(Text("A")          , 'A'               )
  monad_text_to_int         test(Text("1")          , 1                 )
  monad_text_to_long        test(Text("1")          , 1L                )
  monad_text_to_float       test(Text("1.0")        , 1.0f              )
  monad_text_to_double      test(Text("1.0")        , 1.0d              )
  // format: on
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
