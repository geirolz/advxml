package advxml.core

import cats.data._

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

package object data {
  type ValidatedNelEx[+T] = ValidatedNel[Throwable, T]
  type ValidatedEx[+T] = Validated[Throwable, T]
  type EitherEx[+T] = Either[Throwable, T]
  type EitherNelEx[+T] = EitherNel[Throwable, T]
  type ThrowableNel = NonEmptyList[Throwable]
  type XmlPredicate = NodeSeq => Boolean

  /** Syntactic sugar. To use ase `A As B` instead of classic method `Converter[A, B]`
    */
  @implicitNotFound("""Missing implicit Converter instance to covert ${A} into ${B}""")
  type As[-A, B] = Converter[A, B]

  /** Represents a function `A => ValidatedEx[B]` to simplify method and class signatures.
    * Because the conversion can fail the output is wrapped into cats [[ValidatedNelEx]] in order to handle the errors.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound(
    """Missing implicit ValidatedConverter instance to covert ${A} into ValidatedNelEx[${B}]"""
  )
  type ValidatedConverter[-A, B] = Converter[A, ValidatedNelEx[B]]

  /** Represents a function `A => Option[B]` to simplify method and class signatures.
    * Because the conversion can fail the output is wrapped into `Option` in order to handle the empty case.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  @implicitNotFound(
    """Missing implicit OptionConverter instance to covert ${A} into Option[${B}]."""
  )
  type OptionConverter[-A, B] = Converter[A, Option[B]]

  /** This is just an alias for [[advxml.core.data.ValidatedConverter]] parametrized with `NodeSeq` on left side.
    * @tparam T Output object type
    */
  @implicitNotFound(
    """Missing implicit XmlDecoder instance to covert NodeSeq into ValidatedNelEx[${T}]."""
  )
  type XmlDecoder[T] = ValidatedConverter[NodeSeq, T]

  /** This is just an alias for [[advxml.core.data.ValidatedConverter]] parametrized with `NodeSeq` on right side.
    * @tparam T Contravariant input object type
    */
  @implicitNotFound(
    """Missing implicit XmlDecoder instance to covert ${T} into ValidatedNelEx[NodeSeq]."""
  )
  type XmlEncoder[T] = ValidatedConverter[T, NodeSeq]
}
