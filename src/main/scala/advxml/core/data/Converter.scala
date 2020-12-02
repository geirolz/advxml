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
    * @tparam F Effect type
    * @tparam A Input type
    * @tparam B Output type
    * @return Converter instance
    */
  def of[F[_], A, B](f: A => F[B]): Converter[F, A, B] = Kleisli(f)

  /** Create an always pure converter that return the input instance wrapped in `F`.
    * @tparam A input and output type
    * @return Identity [[Converter]] instance
    */
  def id[F[_]: Applicative, A]: Converter[F, A, A] = Converter.of(Applicative[F].pure(_))

  /** Create an always pure converter that return the passed value ignoring the converter input.
    * @param v Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam B inner output type
    * @return Constant [[Converter]] instance
    */
  def const[F[_]: Applicative, A, B](v: B): Converter[F, A, B] = Converter.of(_ => Applicative[F].pure(v))

  /** Apply conversion using implicit [[Converter]] instance.
    * This method catch a [[Converter]] instance in the scope that conforms with types `F`, `A` and `B` and then invoke
    * in it the method `apply` passing `a`.
    *
    * @param F implicit [[Converter]] instance
    * @tparam F Output context
    * @tparam A Contravariant input type
    * @tparam B Output object type
    * @return Safe conversion of `A` into `B`, express as `F[B]`
    */
  def apply[F[_], A, B](implicit F: Converter[F, A, B]): Converter[F, A, B] = F
}

private[core] sealed abstract class FixedWrapperConverter[F[_]: Applicative, C[-A, B] <: Converter[F, A, B]] {

  type Wrapper[_] = F[_]

  def of[A, B](f: A => F[B]): C[A, B] = Converter.of[F, A, B](f).asInstanceOf[C[A, B]]

  def id[A]: C[A, A] = Converter.id.asInstanceOf[C[A, A]]

  def const[A, B](v: B): C[A, B] = Converter.const(v).asInstanceOf[C[A, B]]

  def apply[A, B](implicit F: Converter[F, A, B]): C[A, B] = Converter[F, A, B].asInstanceOf[C[A, B]]
}

object PureConverter extends FixedWrapperConverter[Id, PureConverter]

object ValidatedConverter extends FixedWrapperConverter[ValidatedNelEx, ValidatedConverter]

object XmlConverter {

  /** Syntactic sugar to convert a [[NodeSeq]] instance into `O` using an implicit [[ToXml]] instance.
    * This method catch a [[XmlTo]] instance in the scope that conforms with types `O` and then invoke
    * in it the method `apply` passing `xml`.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam O Object output type
    * @return [[ValidatedNelEx]] instance that, if on success case contains `I` instance.
    */
  def asModel[I <: NodeSeq: XmlTo[*, O], O](xml: I): ValidatedNelEx[O] = ValidatedConverter[I, O].run(xml)

  /** Syntactic sugar to convert a `O` instance into [[NodeSeq]] using an implicit [[XmlTo]] instance.
    *
    * This method catch a [[ToXml]] instance in the scope that conforms with types `I` and then invoke
    * in it the method `apply` passing `model`.
    *
    * See [[ToXml]] for further information.
    *
    * @see [[ValidatedConverter]] for further information.
    * @tparam I Contravariant input model type
    * @return [[ValidatedNelEx]] instance that, if on success case contains `O` instance.
    */
  def asXml[I: ToXml[*, O], O <: NodeSeq](model: I): ValidatedNelEx[O] =
    ValidatedConverter[I, O].run(model)
}
