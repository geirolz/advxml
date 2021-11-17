package advxml

import cats.data.*

import scala.annotation.implicitNotFound
import scala.xml.NodeSeq

package object data {
  type ValidatedNelThrow[+T] = ValidatedNel[Throwable, T]
  type ValidatedThrow[+T]    = Validated[Throwable, T]
  type EitherThrow[+T]       = Either[Throwable, T]
  type EitherNelThrow[+T]    = EitherNel[Throwable, T]
  type ThrowableNel          = NonEmptyList[Throwable]
  type XmlPredicate          = NodeSeq => Boolean

  /** Syntactic sugar. To use ase `A As B` instead of classic method `Converter[A, B]`
    */
  @implicitNotFound("""Missing implicit Converter instance to covert ${A} into ${B}""")
  type As[-A, B] = Converter[A, B]

  /** Represents a function `A => ValidatedThrow[B]` to simplify method and class signatures.
    * Because the conversion can fail the output is wrapped into cats [[ValidatedNelThrow]] in order
    * to handle the errors.
    *
    * @tparam A
    *   Contravariant input object type
    * @tparam B
    *   Output object type
    */
  @implicitNotFound(
    """Missing implicit ValidatedConverter instance to covert ${A} into ValidatedNelThrow[${B}]"""
  )
  type ValidatedConverter[-A, B] = Converter[A, ValidatedNelThrow[B]]

  /** Represents a function `A => Option[B]` to simplify method and class signatures. Because the
    * conversion can fail the output is wrapped into `Option` in order to handle the empty case.
    *
    * @tparam A
    *   Contravariant input object type
    * @tparam B
    *   Output object type
    */
  @implicitNotFound(
    """Missing implicit OptionConverter instance to covert ${A} into Option[${B}]."""
  )
  type OptionConverter[-A, B] = Converter[A, Option[B]]

  /** This is just an alias for [[advxml.data.ValidatedConverter]] parametrized with `NodeSeq` on
    * left side.
    *
    * @tparam T
    *   Output object type
    */
  @implicitNotFound(
    """Missing implicit XmlDecoder instance to covert NodeSeq into ValidatedNelThrow[${T}]."""
  )
  type XmlDecoder[T] = ValidatedConverter[NodeSeq, T]

  /** This is just an alias for [[advxml.data.Converter]] parametrized with `NodeSeq` on right side.
    *
    * @tparam T
    *   Contravariant input object type
    */
  @implicitNotFound(
    """Missing implicit XmlDecoder instance to covert ${T} into `NodeSeq`."""
  )
  type XmlEncoder[T] = Converter[T, NodeSeq]
}
