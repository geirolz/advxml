package advxml.xpath

import advxml.data.Key
import advxml.data.SimpleValue
import advxml.data.XmlPredicate
import advxml.implicits.*
import advxml.xpath.error.NotSupportedConstruction
import cats.Endo
import cats.data.ValidatedNel
import cats.syntax.apply.*
import cats.syntax.foldable.*
import cats.syntax.traverse.*
import cats.syntax.validated.*
import eu.cdevreeze.xpathparser.ast.*

object XmlPredicateBuilder {
  def build(expr: Expr): ValidatedNel[NotSupportedConstruction, XmlPredicate] =
    expr match {
      case expr: CompoundComparisonExpr => buildCCE(expr)
      case expr: RelativePathExpr       => buildRPE(expr).map(_(_ => true))
      case CompoundOrExpr(head, tail) =>
        import advxml.xpath.utils.predicate.or.*
        (build(head), tail.traverse(build(_))).mapN(_ +: _).map(_.combineAll)
      case CompoundAndExpr(head, tail) =>
        import advxml.xpath.utils.predicate.and.*
        (build(head), tail.traverse(build(_))).mapN(_ +: _).map(_.combineAll)
      case e => notSupported(e)
    }

  def buildCCE(expr: CompoundComparisonExpr): ValidatedNel[NotSupportedConstruction, XmlPredicate] =
    expr match {
      case CompoundComparisonExpr(
            cpe @ CompoundOrExact(
              ForwardAxisStep(
                AttributeAxisAbbrevForwardStep(SimpleNameTest(EQNameEx(name))),
                EmptySeq()
              )
            ),
            GeneralComp.Eq,
            StringLiteral(value)
          ) =>
        buildRPE(cpe).map(p => p(XmlPredicate.attrs(Key(name) === value)))

      case CompoundComparisonExpr(
            cpe @ CompoundOrExact(
              ForwardAxisStep(SimpleAbbrevForwardStep(SimpleNameTest(_)), EmptySeq())
            ),
            comp: GeneralComp,
            IntegerLiteral(value)
          ) =>
        buildRPE(cpe).map(p =>
          p(XmlPredicate.text(_.asOption[Int].exists(comp match {
            case GeneralComp.Eq => _ == value.toInt
            case GeneralComp.Ne => _ != value.toInt
            case GeneralComp.Lt => _ < value.toInt
            case GeneralComp.Le => _ <= value.toInt
            case GeneralComp.Gt => _ > value.toInt
            case GeneralComp.Ge => _ >= value.toInt
          })))
        )

      case CompoundComparisonExpr(
            cpe @ CompoundOrExact(ForwardAxisStep(SimpleAbbrevForwardStep(TextTest), EmptySeq())),
            GeneralComp.Eq,
            StringLiteral(value)
          ) =>
        buildRPE(cpe).map(p => p(XmlPredicate.text(_ == value)))

      case e => notSupported(e)
    }

  def buildRPE(
    expr: RelativePathExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlPredicate]] =
    expr match {
      case ForwardAxisStep(SimpleAbbrevForwardStep(SimpleNameTest(EQNameEx(name))), EmptySeq()) =>
        xpred(XmlPredicate.hasImmediateChild(name, _)).validNel

      case CompoundRelativePathExpr(init, StepOp.SingleSlash, lastStep) =>
        (buildRPE(init), buildRPE(lastStep)).mapN { case (init, last) => init.andThen(last) }

      case ForwardAxisStep(SimpleAbbrevForwardStep(TextTest), EmptySeq()) =>
        (identity[XmlPredicate] _).validNel

      case ForwardAxisStep(AttributeAxisAbbrevForwardStep(SimpleNameTest(_)), EmptySeq()) =>
        (identity[XmlPredicate] _).validNel

      case FunctionCall(
            EQNameEx(fn @ ("contains" | "starts-with" | "ends-with")),
            ArgumentList(
              UnSeq(
                ExprSingleArgument(ForwardAxisStep(SimpleAbbrevForwardStep(TextTest), EmptySeq())),
                ExprSingleArgument(StringLiteral(value))
              )
            )
          ) =>
        fn match {
          case "contains"    => const(XmlPredicate.text(_.contains(value))).validNel
          case "starts-with" => const(XmlPredicate.text(_.startsWith(value))).validNel
          case "ends-with"   => const(XmlPredicate.text(_.endsWith(value))).validNel
        }

      case FunctionCall(
            EQNameEx(fn @ ("contains" | "starts-with" | "ends-with")),
            ArgumentList(
              UnSeq(
                ExprSingleArgument(
                  ForwardAxisStep(
                    AttributeAxisAbbrevForwardStep(SimpleNameTest(EQNameEx(attr))),
                    EmptySeq()
                  )
                ),
                ExprSingleArgument(StringLiteral(value))
              )
            )
          ) =>
        fn match {
          case "contains" =>
            const(
              XmlPredicate.attrs(
                Key(attr) -> ((_: SimpleValue).asOption[String].exists(_.contains(value)))
              )
            ).validNel

          case "starts-with" =>
            const(
              XmlPredicate.attrs(
                Key(attr) -> ((_: SimpleValue).asOption[String].exists(_.startsWith(value)))
              )
            ).validNel

          case "ends-with" =>
            const(
              XmlPredicate.attrs(
                Key(attr) -> ((_: SimpleValue).asOption[String].exists(_.endsWith(value)))
              )
            ).validNel
        }

      case FunctionCall(
            EQNameEx("not"),
            ArgumentList(
              UnSeq(
                ExprSingleArgument(fc: FunctionCall)
              )
            )
          ) =>
        buildRPE(fc).map(pred => pred.andThen(_.andThen(!_)))

      case e => notSupported(e)
    }

  private def notSupported(feature: XPathElem): ValidatedNel[NotSupportedConstruction, Nothing] =
    NotSupportedConstruction(feature).invalidNel

  @inline
  private def xpred(f: Endo[XmlPredicate]): Endo[XmlPredicate] = f

  @inline
  private def const(p: XmlPredicate): Endo[XmlPredicate] = _ => p

  private object CompoundOrExact {
    def unapply(expr: RelativePathExpr): Option[StepExpr] =
      expr match {
        case se: StepExpr                   => Some(se)
        case crpe: CompoundRelativePathExpr => Some(crpe.lastStepExpr)
      }
  }
}
