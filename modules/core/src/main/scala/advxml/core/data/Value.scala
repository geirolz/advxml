package advxml.core.data

import advxml.core.AppExOrEu
import cats.{Applicative, Id, PartialOrder, Show}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

import scala.util.matching.Regex

//======================================= VALUES ==================================
sealed trait Value extends AsValidable[ValidatedValue] {
  val ref: Option[String]
}
object Value {

  //instances
  implicit def valueExtractorForValueF[F[_]: AppExOrEu]: ValueExtractor[F, Value] = {
    case v @ SimpleValue(_, _)       => v.extract[F]
    case v @ ValidatedValue(_, _, _) => v.extract[F]
  }

  //embedded syntax
  implicit class ValueExtractorSyntaxOps[V <: Value](value: V) {

    def get(implicit be: ValueExtractor[Id, V]): String =
      be.extract(value)

    def validated(implicit ve: ValueExtractor[ValidatedNelEx, V]): ValidatedNelEx[String] =
      extract[ValidatedNelEx]

    def extract[F[_]](implicit be: ValueExtractor[F, V]): F[String] =
      be.extract(value)
  }
}

case class SimpleValue(private[data] val data: String, ref: Option[String] = None)
    extends Value
    with Comparable[SimpleValue] {

  override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
    ValidatedValue.fromSimpleValue(this, NonEmptyList.of(nrule, nrules: _*))

  override def compareTo(that: SimpleValue): Int = this.data.compareTo(that.data)

  override def toString: String = Show[SimpleValue].show(this)
}
private[data] sealed trait SimpleValueLowPriorityInstances {
  implicit def valueExtractorForSimpleValueF[F[_]: Applicative]: ValueExtractor[F, SimpleValue] =
    (value: SimpleValue) => Applicative[F].pure(value.data)
}
object SimpleValue extends SimpleValueLowPriorityInstances {

  implicit def valueExtractorForSimpleValueId: ValueExtractor[Id, SimpleValue] =
    (value: SimpleValue) => value.data

  implicit val advxmlValueCatsInstances: PartialOrder[SimpleValue] with Show[SimpleValue] =
    new PartialOrder[SimpleValue] with Show[SimpleValue] {
      override def partialCompare(x: SimpleValue, y: SimpleValue): Double = x.data.compareTo(y.data).toDouble
      override def show(t: SimpleValue): String = s"""${t.ref.map(r => s"$r => ").getOrElse("")}"${t.data}""""
    }
}

case class ValidatedValue(private val data: String, rules: NonEmptyList[ValidationRule], ref: Option[String] = None)
    extends Value {

  def toSimpleValue: SimpleValue = SimpleValue(data, ref)

  override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
    copy(rules = rules.concatNel(NonEmptyList.of(nrule, nrules: _*)))

  override def toString: String = Show[SimpleValue].show(toSimpleValue)
}
object ValidatedValue {

  def fromSimpleValue(simpleValue: SimpleValue, rules: NonEmptyList[ValidationRule]): ValidatedValue =
    ValidatedValue(simpleValue.get, rules, simpleValue.ref)

  implicit def valueExtractorForValidatedValueF[F[_]: AppExOrEu]: ValueExtractor[F, ValidatedValue] =
    (vvalue: ValidatedValue) => {
      import cats.implicits._

      val result: Validated[ValidationRule.Errors, SimpleValue] = vvalue.rules
        .map(validationRule => validationRule(vvalue.toSimpleValue).toValidatedNel)
        .toList
        .reduce((a, b) => a.productL[SimpleValue](b))
        .swap
        .map(errors => ValidationRule.Errors(vvalue, errors))
        .swap

      result match {
        case Valid(a)   => AppExOrEu[F].pure(a.get)
        case Invalid(e) => AppExOrEu[F].raiseErrorOrEmpty(e.exception)
      }
    }
}

//======================================= TYPE CLASS ==================================
trait ValueExtractor[F[_], V <: Value] {
  def extract(value: V): F[String]
}
object ValueExtractor {
  def apply[F[_], V <: Value](implicit ve: ValueExtractor[F, V]): ValueExtractor[F, V] = ve
}

//=============================== VALIDATION RULE ===============================
trait AsValidable[V] {

  import advxml.instances.data.value._

  def validate(nrule: ValidationRule, nrules: ValidationRule*): V

  def nonEmpty: V = validate(NonEmpty)

  def matchRegex(regex: Regex): V = validate(MatchRegex(regex))
}

class ValidationRule(val name: String, val validator: String => Boolean, val errorReason: String) {

  final def apply(v: SimpleValue): Validated[ValidationRule.Error, SimpleValue] = {
    validator(v.get) match {
      case true  => Valid(v)
      case false => Invalid(ValidationRule.Error(this, errorReason))
    }
  }
}

object ValidationRule {

  def apply(name: String)(validator: String => Boolean, errorReason: => String): ValidationRule =
    new ValidationRule(name, validator, errorReason)

  case class Error(rule: ValidationRule, reason: String)

  case class Errors(value: ValidatedValue, errors: NonEmptyList[Error]) {

    lazy val result: NonEmptyList[Either[Error, ValidationRule]] = {

      val success = value.rules
        .filterNot(r => errors.map(_.rule).toList.contains(r))
        .map(a => Right[Error, ValidationRule](a))

      val failure = errors
        .map(e => Left[Error, ValidationRule](e))

      failure.concat(success)
    }

    lazy val report: String = {

      def ifNonEmpty(list: List[(String, Boolean)], str: String): String =
        if (list.nonEmpty) s"- $str(${list.size})\n${list.map(_._1).mkString("\n")}" else ""

      def ruleCheckStr(rule: ValidationRule, isSuccess: Boolean, reason: String = ""): String =
        s"[${if (isSuccess) "✓" else " "}] ${rule.name}${if (reason.nonEmpty) s": $reason." else ""}"

      val (success, failed): (List[(String, Boolean)], List[(String, Boolean)]) = result
        .map {
          case Left(error) => ruleCheckStr(error.rule, isSuccess = false, error.reason) -> false
          case Right(rule) => ruleCheckStr(rule, isSuccess = true)                      -> true
        }
        .toList
        .partition(t => t._2)

      s"""
         |# Value: $value
         |${ifNonEmpty(failed, "FAILED RULES")}
         |-------------------------------------
         |${ifNonEmpty(success, "SUCCESS RULES")}
         |""".stripMargin
    }

    lazy val exception: RuntimeException = new RuntimeException(report)
  }
}
