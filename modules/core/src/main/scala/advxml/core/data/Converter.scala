package advxml.core.data

import cats.{Applicative, Id}
import cats.data.Kleisli

import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 31/10/2019.
  *
  * @author geirolad
  */

/** Represents a function `A => B` to simplify method and class signatures.
  * This alias represent a converter to transform `A` into `B`.
  *
  * @tparam A Contravariant input object type
  * @tparam B Output object type
  */
trait Converter[-A, B] {

  def run(a: A): B

  def map[U](f: B => U): Converter[A, U] =
    Converter.of(a => f(run(a)))

  def contramap[AA](f: AA => A): Converter[AA, B] =
    Converter.of(aa => run(f(aa)))

  def asKleisli: Kleisli[Id, A, B] = Kleisli[Id, A, B](run)
}

object Converter {

  /** Create an instance of [[Converter]]
    * @param f function to map input to output
    * @tparam A Input type
    * @tparam B Output type
    * @return Converter instance
    */
  def of[A, B](f: A => B): Converter[A, B] = (a: A) => f(a)

  /** Create an always pure converter that return the input instance.
    * @tparam A input and output type
    * @return Identity [[Converter]] instance
    */
  def id[A]: Converter[A, A] = Converter.of(a => a)

  /** Create an always pure converter that return the passed value ignoring the converter input.
    * @param b Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam B inner output type
    * @return Constant [[Converter]] instance
    */
  def pure[A, B](b: B): Converter[A, B] = Converter.of(_ => b)

  /** Apply conversion using implicit [[Converter]] instance.
    * This method catch a [[Converter]] instance in the scope that conforms with types `F`, `A` and `B` and then invoke
    * in it the method `apply` passing `a`.
    *
    * @param c implicit [[Converter]] instance
    * @tparam A Contravariant input type
    * @tparam B Output object type
    * @return Safe conversion of `A` into `B`, express as `F[B]`
    */
  def apply[A, B](implicit c: Converter[A, B]): Converter[A, B] = c
}

//=================================== HELPERS ============================================
private[core] sealed abstract class FixedBiConverterOps[F[_]: Applicative, C[-A, B] <: Converter[A, F[B]]] {
  def of[A, B](f: A => F[B]): C[A, B] = Converter.of[A, F[B]](f).asInstanceOf[C[A, B]]
  def pure[A, B](b: B): C[A, B] = Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[C[A, B]]
  def apply[A, B](implicit F: Converter[A, F[B]]): C[A, B] = Converter[A, F[B]].asInstanceOf[C[A, B]]
}

private[core] sealed abstract class FixedLeftConverterOps[F[_]: Applicative, A, CR[B] <: Converter[A, F[B]]] {
  def of[B](f: A => F[B]): CR[B] = Converter.of[A, F[B]](f).asInstanceOf[CR[B]]
  def pure[B](b: B): CR[B] = Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[CR[B]]
  def apply[B](implicit F: Converter[A, F[B]]): CR[B] = Converter[A, F[B]].asInstanceOf[CR[B]]
}

private[core] sealed abstract class FixedRightConverterOps[F[_]: Applicative, B, CL[A] <: Converter[A, F[B]]] {
  def of[A](f: A => F[B]): CL[A] = Converter.of[A, F[B]](f).asInstanceOf[CL[A]]
  def pure[A](b: B): CL[A] = Converter.pure[A, F[B]](Applicative[F].pure(b)).asInstanceOf[CL[A]]
  def apply[A](implicit F: Converter[A, F[B]]): CL[A] = Converter[A, F[B]].asInstanceOf[CL[A]]
}

object ValidatedConverter extends FixedBiConverterOps[ValidatedNelThrow, ValidatedConverter]

object OptionConverter extends FixedBiConverterOps[Option, OptionConverter]

object XmlDecoder extends FixedLeftConverterOps[ValidatedNelThrow, NodeSeq, XmlDecoder]

object XmlEncoder extends FixedRightConverterOps[ValidatedNelThrow, NodeSeq, XmlEncoder]
