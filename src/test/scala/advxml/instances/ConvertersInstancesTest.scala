package advxml.instances

import advxml.core.convert.Converter
import cats.Monad
import org.scalatest.funsuite.AnyFunSuite

import scala.xml.Text

class FromText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import advxml.instances.convert._
  import cats.instances.try_._

  // format: off
  string_to_text      test("TEST"             , Text("TEST")  )
  bigInt_to_text      test(BigInt(1)          , Text("1")     )
  bigDecimal_to_text  test(BigDecimal(1.1234) , Text("1.1234"))
  byte_to_text        test(1.toByte           , Text("1")     )
  short_to_text       test(1.toShort          , Text("1")     )
  char_to_text        test('A'                , Text("A")     )
  int_to_text         test(1                  , Text("1")     )
  long_to_text        test(1L                 , Text("1")     )
  float_to_text       test(1.0f               , Text("1.0")   )
  double_to_text      test(1.0d               , Text("1.0")   )
  // format: on
}

class ToText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import advxml.instances.convert._
  import cats.instances.try_._

  // format: off
  text_to_string      test(Text("TEST")       , "TEST"            )
  text_to_bigInt      test(Text("1")          , BigInt(1))
  text_to_bigDecimal  test(Text("1.1234")     , BigDecimal(1.1234))
  text_to_byte        test(Text("1")          , 1.toByte          )
  text_to_short       test(Text("1")          , 1.toShort         )
  text_to_char        test(Text("A")          , 'A'               )
  text_to_int         test(Text("1")          , 1                 )
  text_to_long        test(Text("1")          , 1L                )
  text_to_float       test(Text("1.0")        , 1.0f              )
  text_to_double      test(Text("1.0")        , 1.0d              )
  // format: on
}

private[instances] sealed trait ConvertersAssertsUtils { $this: AnyFunSuite =>

  import scala.reflect.runtime.universe._

  protected implicit class TestConverterOps[F[_]: Monad, I: TypeTag, O: TypeTag](converter: Converter[F, I, O]) {
    def test(in: I, expectedOut: O): Unit = {
      $this.test(
        s"Converter[${typeOf[I].finalResultType}, ${typeOf[O].finalResultType}]" +
        s".apply('$in') should be '$expectedOut'"
      ) {
        Monad[F].map(converter(in))(v => assert(v == expectedOut))
      }
    }
  }
}
