package advxml.core.transform

import advxml.core.MonadEx
import scala.xml.NodeSeq
import cats.syntax.all._

trait AbstractRule
case class And(a: AbstractRule, b: AbstractRule) extends AbstractRule
case class OrElse(a: AbstractRule, b: AbstractRule) extends AbstractRule

object AbstractRule {

  def transform[F[_]](root: NodeSeq, rule: AbstractRule, rules: AbstractRule*)(implicit F: MonadEx[F]): F[NodeSeq] =
    transform(root, List(rule) ++ rules)

  def transform[F[_]](root: NodeSeq, rules: List[AbstractRule])(implicit F: MonadEx[F]): F[NodeSeq] =
    rules.foldLeft(F.pure(root))((actDoc, rule) => actDoc.flatMap(doc => transform(doc, rule)))

  def transform[F[_]](doc: NodeSeq, rule: AbstractRule)(implicit F: MonadEx[F]): F[NodeSeq] =
    rule match {
      case OrElse(a, b)  => F.handleErrorWith(transform(doc, a))(_ => transform(doc, b))
      case And(a, b)     => transform(doc, a).flatMap(transform(_, b))
      case rule: XmlRule => XmlRule.transform(doc, rule)
    }

}
