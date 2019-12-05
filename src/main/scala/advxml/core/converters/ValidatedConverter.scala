package advxml.core.converters

import advxml.core.validation.ValidatedEx

import scala.annotation.implicitNotFound

/**
  * Advxml
  * Created by geirolad on 31/10/2019.
  *
  * @author geirolad
  */
object ValidatedConverter {

  /**
    * Create an always valid converter that return the input instance.
    *
    * @see [[advxml.core.converters.Converter]] for further information.
    * @tparam A Endo converter input and output type
    * @return Identity [[ValidatedConverter]] instance
    */
  def id[A]: ValidatedConverter[A, A] = Converter.id[ValidatedEx, A]

  /**
    * Create an always valid converter that return the passed value ignoring the converter input.
    *
    * @see [[advxml.core.converters.Converter]] for further information.
    * @param v Value returned when the [[ValidatedConverter]] is invoked, the converter input is ignored.
    * @tparam B Convert output type
    * @return Constant [[ValidatedConverter]] instance
    */
  def const[A, B](v: B): ValidatedConverter[A, B] = Converter.const[ValidatedEx, A, B](v)

  /**
    * Apply conversion using implicit [[ValidatedConverter]] instance.
    *
    * @see [[advxml.core.converters.Converter]] for further information.
    * @param a Input instance
    * @param F implicit [[ValidatedConverter]] instance
    * @tparam A Contravariant input type
    * @tparam B Output object type
    * @return Safe conversion of `A` into `B`, express as `ValidatedEx[B]`
    */
  @implicitNotFound("Missing ValidatedConverter to transform ${A} into ValidatedEx[${B}]")
  def apply[A, B](a: A)(implicit F: ValidatedConverter[A, B]): ValidatedEx[B] = F.apply(a)
}
