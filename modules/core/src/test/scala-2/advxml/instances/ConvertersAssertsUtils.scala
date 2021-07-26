package advxml.instances

import advxml.core.data.Converter
import cats.Id
import org.scalatest.funsuite.AnyFunSuite

import scala.reflect.runtime.universe._

private[instances] trait ConvertersAssertsUtils { $this: AnyFunSuite =>
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
