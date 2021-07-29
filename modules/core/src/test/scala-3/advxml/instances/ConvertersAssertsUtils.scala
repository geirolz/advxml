package advxml.instances

import advxml.core.data.Converter
import cats.Id
import org.scalatest.funsuite.AnyFunSuite

import scala.quoted.*

private def showTypeImpl[T: Type](using Quotes): Expr[String] = {
  import quotes.reflect.*
  Expr(TypeRepr.of[T].show)
}

private inline def showType[T]: String = ${ showTypeImpl[T] }

private[instances] trait ConvertersAssertsUtils { $this: AnyFunSuite =>
  extension [I, O](converter: Converter[I, O]) {
    inline def test(in: I, expectedOut: O): Unit = {
      $this.test(s"Converter[${showType[I]}, ${showType[O]}].apply('$in') should be '$expectedOut'") {
        assert(converter.run(in) == expectedOut)
      }
    }
  }
}
