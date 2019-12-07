package advxml.core.convert

import cats.Applicative

import scala.annotation.implicitNotFound
import scala.xml.Text

/**
  * Advxml
  * Created by geirolad on 03/07/2019.
  *
  * @author geirolad
  */
object TextConverter {

  def mapAsText[F[_]: Applicative, A](fa: F[A])(implicit s: TextConverter[A]): F[Text] =
    Applicative[F].map(fa)(apply(_))

  /**
    * Apply conversion using implicit [[TextConverter]] instance.
    *
    * @see [[Converter]] for further information.
    * @param a Input instance
    * @param F implicit [[TextConverter]] instance
    * @tparam A Contravariant input type
    * @return Unsafe conversion of `A` into `Text`
    */
  @implicitNotFound("Missing TextConverter to transform ${A} into `Text`")
  def apply[A](a: A)(implicit F: TextConverter[A]): Text = F(a)
}
