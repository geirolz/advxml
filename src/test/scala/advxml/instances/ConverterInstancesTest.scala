package advxml.instances

import advxml.core.data.Converter
import cats.{Id, Monad}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Try

import advxml.instances.convert._

class Common_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  implicitly[Converter[Try, Int, Int]].test(1, 1)
  implicitly[Converter[Id,  Int, Int]].test(1, 1)
  // format: on
}

class ToText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {
  // format: off
  id_bigInt_to_str      test(BigInt(1)          , "1"     )
  id_bigDecimal_to_str  test(BigDecimal(1.1234) , "1.1234")
  id_byte_to_str        test(1.toByte           , "1"     )
  id_short_to_str       test(1.toShort          , "1"     )
  id_char_to_str        test('A'                , "A"     )
  id_int_to_str         test(1                  , "1"     )
  id_long_to_str        test(1L                 , "1"     )
  id_float_to_str       test(1.0f               , "1.0"   )
  id_double_to_str      test(1.0d               , "1.0"   )
  // format: on
}

class FromText_ConvertersInstancesTest extends AnyFunSuite with ConvertersAssertsUtils {

  import cats.instances.try_._

  // format: off
  id_str_to_bigInt         test("1"       , BigInt(1)         )
  id_str_to_bigDecimal     test("1.1234"  , BigDecimal(1.1234))
  id_str_to_byte           test("1"       , 1.toByte          )
  id_str_to_short          test("1"       , 1.toShort         )
  id_str_to_char           test("A"       , 'A'               )
  id_str_to_int            test("1"       , 1                 )
  id_str_to_long           test("1"       , 1L                )
  id_str_to_float          test("1.0"     , 1.0f              )
  id_str_to_double         test("1.0"     , 1.0d              )
  
  monad_str_to_bigInt     [Try] test("1"       , BigInt(1)         )
  monad_str_to_bigDecimal [Try] test("1.1234"  , BigDecimal(1.1234))
  monad_str_to_byte       [Try] test("1"       , 1.toByte          )
  monad_str_to_short      [Try] test("1"       , 1.toShort         )
  monad_str_to_char       [Try] test("A"       , 'A'               )
  monad_str_to_int        [Try] test("1"       , 1                 )
  monad_str_to_long       [Try] test("1"       , 1L                )
  monad_str_to_float      [Try] test("1.0"     , 1.0f              )
  monad_str_to_double     [Try] test("1.0"     , 1.0d              )
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
