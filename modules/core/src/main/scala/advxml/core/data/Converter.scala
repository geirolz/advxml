package advxml.core.data

import cats.{Applicative, Id}
import cats.data.Kleisli

import scala.xml.NodeSeq

/** Advxml
  * Created by geirolad on 31/10/2019.
  *
  * @author geirolad
  */
object Converter {

  /** Create an instance of [[Converter]]
    * @param f function to map input to output
    * @tparam A Input type
    * @tparam B Output type
    * @return Converter instance
    */
  def of[A, B](f: A => B): Converter[A, B] = Kleisli[Id, A, B](f)

  /** Create an always pure converter that return the input instance.
    * @tparam A input and output type
    * @return Identity [[Converter]] instance
    */
  def id[A]: Converter[A, A] = Converter.of(a => a)

  /** Create an always pure converter that return the input instance wrapped in `F`.
    * @tparam F context
    * @tparam A input and output type
    * @return Identity [[Converter]] instance
    */
  def idF[F[_]: Applicative, A]: Converter[A, F[A]] = Converter.of(a => Applicative[F].pure(a))

  /** Create an always pure converter that return the passed value ignoring the converter input.
    * @param b Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam B inner output type
    * @return Constant [[Converter]] instance
    */
  def const[A, B](b: B): Converter[A, B] = Converter.of(_ => b)

  /** Create an always pure converter that return the passed value in F[_] ignoring the converter input.
    * @param b Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam F context
    * @tparam B inner output type
    * @return Constant [[Converter]] instance
    */
  def constF[F[_]: Applicative, A, B](b: B): Converter[A, F[B]] = Converter.of(_ => Applicative[F].pure(b))

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
  def id[A]: C[A, A] = Converter.idF[F, A].asInstanceOf[C[A, A]]
  def const[A, B](b: B): C[A, B] = Converter.constF(b).asInstanceOf[C[A, B]]
  def apply[A, B](implicit F: Converter[A, F[B]]): C[A, B] = Converter[A, F[B]].asInstanceOf[C[A, B]]
}

private[core] sealed abstract class FixedLeftConverterOps[F[_]: Applicative, A, CR[B] <: Converter[A, F[B]]] {
  def of[B](f: A => F[B]): CR[B] = Converter.of[A, F[B]](f).asInstanceOf[CR[B]]
  def id: CR[A] = Converter.idF[F, A].asInstanceOf[CR[A]]
  def const[B](b: B): CR[B] = Converter.constF[F, A, B](b).asInstanceOf[CR[B]]
  def apply[B](implicit F: Converter[A, F[B]]): CR[B] = Converter[A, F[B]].asInstanceOf[CR[B]]
}

private[core] sealed abstract class FixedRightConverterOps[F[_]: Applicative, B, CL[A] <: Converter[A, F[B]]] {
  def of[A](f: A => F[B]): CL[A] = Converter.of[A, F[B]](f).asInstanceOf[CL[A]]
  def id: CL[B] = Converter.idF[F, B].asInstanceOf[CL[B]]
  def const[A](b: B): CL[A] = Converter.constF[F, A, B](b).asInstanceOf[CL[A]]
  def apply[A](implicit F: Converter[A, F[B]]): CL[A] = Converter[A, F[B]].asInstanceOf[CL[A]]
}

object ValidatedConverter extends FixedBiConverterOps[ValidatedNelEx, ValidatedConverter]

object OptionConverter extends FixedBiConverterOps[Option, OptionConverter]

object XmlDecoder extends FixedLeftConverterOps[ValidatedNelEx, NodeSeq, XmlDecoder]

object XmlEncoder extends FixedRightConverterOps[ValidatedNelEx, NodeSeq, XmlEncoder]
