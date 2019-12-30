package advxml.core.transform

import advxml.core.validate.MonadEx

import scala.xml.NodeSeq
import scala.xml.transform.{BasicTransformer, RewriteRule, RuleTransformer}

object XmlTransformer {

  def transform[F[_]: MonadEx](root: NodeSeq, rules: Seq[XmlRule]): F[NodeSeq] =
    transform(new RuleTransformer(_: _*))(root, rules)

  def transform[F[_]: MonadEx](
    f: Seq[RewriteRule] => BasicTransformer
  )(root: NodeSeq, rules: Seq[XmlRule]): F[NodeSeq] = {

    import cats.implicits._

    rules
      .map(_.toRewriteRule[F](root))
      .toList
      .sequence
      .map(f(_).transform(root))
  }
}
