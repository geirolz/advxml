package advxml.xpath

import advxml.transform.XmlZoom
import advxml.xpath.error.NotSupportedConstruction
import cats.Endo
import cats.data.ValidatedNel
import cats.syntax.validated.*
import eu.cdevreeze.xpathparser.ast.*
import eu.cdevreeze.xpathparser.common.PrefixedName
import eu.cdevreeze.xpathparser.common.UnprefixedName

object XmlZoomBuilder {

  private def notSupported(feature: XPathElem): ValidatedNel[NotSupportedConstruction, Nothing] =
    NotSupportedConstruction(feature).invalidNel

  def modifyZoom(xPathExpr: XPathExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    xPathExpr match {
      case expr: Expr => modifyZoom(expr)
    }

  def modifyZoom(test: NodeTest): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    test match {
      case test: KindTest =>
        notSupported(test)
      case test: NameTest =>
        test match {
          case snt @ SimpleNameTest(name) =>
            name match {
              case EQName.QName(qname) =>
                qname match {
                  case UnprefixedName(localPart) => zoom(_.down(localPart)).validNel
                  case PrefixedName(_, _)        => notSupported(snt)
                }
              case EQName.URIQualifiedName(_) => notSupported(snt)
            }
          case wildcard: Wildcard =>
            wildcard match {
              case AnyWildcard                => zoom(_.down("_")).validNel
              case pw @ PrefixWildcard(_)     => notSupported(pw)
              case lnw @ LocalNameWildcard(_) => notSupported(lnw)
              case nw @ NamespaceWildcard(_)  => notSupported(nw)
            }
        }
    }

  def modifyZoom(step: ForwardStep): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    step match {
      case nafs @ NonAbbrevForwardStep(_, _) => notSupported(nafs)
      case step: AbbrevForwardStep =>
        step match {
          case SimpleAbbrevForwardStep(nodeTest)         => modifyZoom(nodeTest)
          case aaafs @ AttributeAxisAbbrevForwardStep(_) => notSupported(aaafs)
        }
    }

  def modifyZoom(step: AxisStep): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    step match {
      case ForwardAxisStep(step, predicateList) =>
        predicateList.foldLeft(modifyZoom(step)) { case (res, ex) =>
          res.andThen(f => modifyZoom(ex).map(f.andThen))
        }
      case ras @ ReverseAxisStep(_, _) => notSupported(ras)
    }

  def modifyZoom(expr: Expr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    expr match {
      case simpleExpr: SimpleExpr  => modifyZoom(simpleExpr)
      case ce @ CompoundExpr(_, _) => notSupported(ce)
    }

  def modifyZoom(simpleExpr: SimpleExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    simpleExpr match {
      case single: ExprSingle =>
        single match {
          case fe @ ForExpr(_, _)           => notSupported(fe)
          case le @ LetExpr(_, _)           => notSupported(le)
          case qe @ QuantifiedExpr(_, _, _) => notSupported(qe)
          case ie @ IfExpr(_, _, _)         => notSupported(ie)
          case orExpr: OrExpr               => modifyZoom(orExpr)
        }
    }

  def modifyZoom(orExpr: OrExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    orExpr match {
      case simpleOrExpr: SimpleOrExpr =>
        simpleOrExpr match {
          case andExpr: AndExpr => modifyZoom(andExpr)
        }
      case coe @ CompoundOrExpr(_, _) => notSupported(coe)
    }

  def modifyZoom(andExpr: AndExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    andExpr match {
      case simpleAndExpr: SimpleAndExpr =>
        simpleAndExpr match {
          case comparisonExpr: ComparisonExpr => modifyZoom(comparisonExpr)
        }
      case cae @ CompoundAndExpr(_, _) => notSupported(cae)
    }

  def modifyZoom(
    comparisonExpr: ComparisonExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    comparisonExpr match {
      case simpleComparisonExpr: SimpleComparisonExpr =>
        simpleComparisonExpr match {
          case stringConcatExpr: StringConcatExpr => modifyZoom(stringConcatExpr)
        }

      case cce @ CompoundComparisonExpr(_, _, _) =>
        notSupported(cce)
    }

  def modifyZoom(
    stringConcatExpr: StringConcatExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    stringConcatExpr match {
      case simpleStringConcatExpr: SimpleStringConcatExpr =>
        simpleStringConcatExpr match {
          case rangeExpr: RangeExpr => modifyZoom(rangeExpr)
        }
      case csce @ CompoundStringConcatExpr(_, _) => notSupported(csce)
    }

  def modifyZoom(rangeExpr: RangeExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    rangeExpr match {
      case simpleRangeExpr: SimpleRangeExpr =>
        simpleRangeExpr match {
          case additiveExpr: AdditiveExpr => modifyZoom(additiveExpr)
        }
      case cre @ CompoundRangeExpr(_, _) => notSupported(cre)
    }

  def modifyZoom(
    additiveExpr: AdditiveExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    additiveExpr match {
      case simpleAdditiveExpr: SimpleAdditiveExpr =>
        simpleAdditiveExpr match {
          case multiplicativeExpr: MultiplicativeExpr => modifyZoom(multiplicativeExpr)
        }
      case cae @ CompoundAdditiveExpr(_, _, _) =>
        notSupported(cae)
    }

  def modifyZoom(
    multiplicativeExpr: MultiplicativeExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    multiplicativeExpr match {
      case simpleMultiplicativeExpr: SimpleMultiplicativeExpr =>
        simpleMultiplicativeExpr match {
          case unionExpr: UnionExpr => modifyZoom(unionExpr)
        }
      case cme @ CompoundMultiplicativeExpr(_, _, _) =>
        notSupported(cme)
    }

  def modifyZoom(unionExpr: UnionExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    unionExpr match {
      case simpleUnionExpr: SimpleUnionExpr =>
        simpleUnionExpr match {
          case intersectExceptExpr: IntersectExceptExpr => modifyZoom(intersectExceptExpr)
        }
      case cue @ CompoundUnionExpr(_, _) => notSupported(cue)
    }

  def modifyZoom(
    intersectExceptExpr: IntersectExceptExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    intersectExceptExpr match {
      case simpleIntersectExceptExpr: SimpleIntersectExceptExpr =>
        simpleIntersectExceptExpr match {
          case instanceOfExpr: InstanceOfExpr => modifyZoom(instanceOfExpr)
        }
      case ciee @ CompoundIntersectExceptExpr(_, _, _) =>
        notSupported(ciee)
    }

  def modifyZoom(
    instanceOfExpr: InstanceOfExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    instanceOfExpr match {
      case simpleInstanceOfExpr: SimpleInstanceOfExpr =>
        simpleInstanceOfExpr match {
          case treatExpr: TreatExpr => modifyZoom(treatExpr)
        }
      case cioe @ CompoundInstanceOfExpr(_, _) => notSupported(cioe)
    }

  def modifyZoom(treatExpr: TreatExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    treatExpr match {
      case simpleTreatExpr: SimpleTreatExpr =>
        simpleTreatExpr match {
          case castableExpr: CastableExpr => modifyZoom(castableExpr)
        }
      case cte @ CompoundTreatExpr(_, _) => notSupported(cte)
    }

  def modifyZoom(
    castableExpr: CastableExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    castableExpr match {
      case simpleCastableExpr: SimpleCastableExpr =>
        simpleCastableExpr match {
          case castExpr: CastExpr => modifyZoom(castExpr)
        }
      case cce @ CompoundCastableExpr(_, _) => notSupported(cce)
    }

  def modifyZoom(castExpr: CastExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    castExpr match {
      case simpleCastExpr: SimpleCastExpr =>
        simpleCastExpr match {
          case arrowExpr: ArrowExpr => modifyZoom(arrowExpr)
        }
      case cce @ CompoundCastExpr(_, _) => notSupported(cce)
    }

  def modifyZoom(arrowExpr: ArrowExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    arrowExpr match {
      case simpleArrowExpr: SimpleArrowExpr =>
        simpleArrowExpr match {
          case unaryExpr: UnaryExpr => modifyZoom(unaryExpr)
        }
      case cae @ CompoundArrowExpr(_, _) => notSupported(cae)
    }

  def modifyZoom(unaryExpr: UnaryExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    unaryExpr match {
      case simpleUnaryExpr: SimpleUnaryExpr =>
        simpleUnaryExpr match {
          case valueExpr: ValueExpr => modifyZoom(valueExpr)
        }
      case cue @ CompoundUnaryExpr(_, _) => notSupported(cue)
    }

  def modifyZoom(valueExpr: ValueExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    valueExpr match {
      case simpleMapExpr: SimpleMapExpr =>
        simpleMapExpr match {
          case simpleSimpleMapExpr: SimpleSimpleMapExpr =>
            simpleSimpleMapExpr match {
              case pathExpr: PathExpr => modifyZoom(pathExpr)
            }
          case csme @ CompoundSimpleMapExpr(_, _) => notSupported(csme)
        }
    }

  def modifyZoom(pathExpr: PathExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    pathExpr match {
      case SlashOnlyPathExpr                                 => identity[XmlZoom].validNel
      case PathExprStartingWithSingleSlash(relativePathExpr) => modifyZoom(relativePathExpr)
      case peswds @ PathExprStartingWithDoubleSlash(_)       => notSupported(peswds)
      case relativePathExpr: RelativePathExpr                => modifyZoom(relativePathExpr)
    }

  def modifyZoom(
    relativePathExpr: RelativePathExpr
  ): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    relativePathExpr match {
      case simpleRelativePathExpr: SimpleRelativePathExpr =>
        simpleRelativePathExpr match {
          case stepExpr: StepExpr => modifyZoom(stepExpr)
        }

      case crpe @ CompoundRelativePathExpr(_, StepOp.DoubleSlash, _) =>
        notSupported(crpe)

      case CompoundRelativePathExpr(init, StepOp.SingleSlash, lastStepExpr) =>
        modifyZoom(init).andThen(i => modifyZoom(lastStepExpr).map(i.andThen))
    }

  def modifyZoom(stepExpr: StepExpr): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    stepExpr match {
      case postfixExpr: PostfixExpr => notSupported(postfixExpr)
      case axisStep: AxisStep       => modifyZoom(axisStep)
    }

  def modifyZoom(postfix: Postfix): ValidatedNel[NotSupportedConstruction, Endo[XmlZoom]] =
    postfix match {
      case Predicate(IntegerLiteral(v)) => zoom(_.atIndex(v.toInt)).validNel
      case Predicate(FunctionCall(EQNameEx("last"), ArgumentList(EmptySeq()))) =>
        zoom(_.last()).validNel
      case Predicate(expr)       => XmlPredicateBuilder.build(expr).map(p => zoom(_.filter(p)))
      case al @ ArgumentList(_)  => notSupported(al)
      case pl @ PostfixLookup(_) => notSupported(pl)
    }

  @inline
  private def zoom(f: Endo[XmlZoom]): Endo[XmlZoom] = f
}
