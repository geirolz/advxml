package com.github.geirolz.advxml.convert.impls

import cats.{Applicative, Id}

import scala.annotation.implicitNotFound

/**
  * Advxml
  * Created by geirolad on 31/10/2019.
  *
  * @author geirolad
  */
object Converter {

  /**
    * Represents a function `A => F[B]` to simplify method and class signatures.
    * This alias represent an error-handled converter to transform `A` into `B` safely.
    * Because the conversion can fail the output is wrapped into `F` in order to handle the errors.
    *
    * @tparam F Output context
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  type Converter[F[_], -A, B] = A => F[B]

  /**
    * Represents a function `A => B` to simplify method and class signatures.
    * This alias represent an unsafe converter to transform `A` into `B`.
    *
    * The invocation of this function can fail and/or throw an runtime exception.
    *
    * @tparam A Contravariant input object type
    * @tparam B Output object type
    */
  type UnsafeConverter[-A, B] = Converter[Id, A, B]

  /**
    * Create an always pure converter that return the input instance wrapped in `F`.
    * @tparam A input and output type
    * @return Identity [[Converter]] instance
    */
  @implicitNotFound("Missing Applicative instance for ${F}, used to create a pure value of ${A}")
  def id[F[_]: Applicative, A]: Converter[F, A, A] = Applicative[F].pure(_)

  /**
    * Create an always safe converter that return the input instance.
    *
    * @tparam A input and output type
    * @return Identity [[com.github.geirolz.advxml.convert.impls.Converter.UnsafeConverter]] instance
    */
  def unsafeId[A]: UnsafeConverter[A, A] = identity[A]

  /**
    * Create an always pure converter that return the passed value ignoring the converter input.
    * @param v Inner value returned when the [[Converter]] is invoked, the converter input is ignored.
    * @tparam B inner output type
    * @return Constant [[Converter]] instance
    */
  @implicitNotFound("Missing Applicative instance for ${F}, used to create a pure value of ${A}")
  def const[F[_]: Applicative, A, B](v: B): Converter[F, A, B] = _ => Applicative[F].pure(v)

  /**
    * Create an always safe converter that return the passed value ignoring the converter input.
    * @param v Inner value returned when the [[UnsafeConverter]] is invoked, the converter input is ignored.
    * @tparam B inner output type
    * @return Constant [[UnsafeConverter]] instance
    */
  def unsafeConst[A, B](v: B): UnsafeConverter[A, B] = _ => v

  /**
    * Apply conversion using implicit [[Converter]] instance.
    * This method catch a [[Converter]] instance in the scope that conforms with types `F`, `A` and `B` and then invoke
    * in it the method `apply` passing `a`.
    *
    * @param a Input instance
    * @param F implicit [[Converter]] instance
    * @tparam F Output context
    * @tparam A Contravariant input type
    * @tparam B Output object type
    * @return Safe conversion of `A` into `B`, express as `F[B]`
    */
  @implicitNotFound("Missing Converter to transform ${A} into ${F} of ${B}")
  def apply[F[_], A, B](a: A)(implicit F: Converter[F, A, B]): F[B] = F.apply(a)
}
