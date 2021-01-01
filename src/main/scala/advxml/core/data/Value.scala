package advxml.core.data

import advxml.core.AppExOrEu
import cats.{PartialOrder, Show}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

import scala.util.matching.Regex

sealed trait Value extends Comparable[Value] with Serializable {

  import advxml.instances.data.value._

  def unboxed: String

  def extract[F[_]: AppExOrEu]: F[String] =
    this match {
      case value: ValidatedValue => value.extract[F]
      case value: Value          => AppExOrEu[F].pure(value.unboxed)
    }

  def ref: Option[String]

  def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue

  def nonEmpty: ValidatedValue = validate(NonEmpty)

  def matchRegex(regex: Regex): ValidatedValue = validate(MatchRegex(regex))

  override def compareTo(that: Value): Int = this.unboxed.compareTo(that.unboxed)
}

sealed trait ValidatedValue extends Value {

  def toValue: Value = Value(unboxed)

  def rules: NonEmptyList[ValidationRule]

  override def extract[F[_]: AppExOrEu]: F[String] = {

    import cats.implicits._

    val result: Validated[ValidationRule.Errors, ValidatedValue] = rules
      .map(r => r(this).toValidatedNel)
      .toList
      .reduce((a, b) => a.productL[ValidatedValue](b))
      .swap
      .map(errors => ValidationRule.Errors(this, errors))
      .swap

    result match {
      case Valid(a)   => AppExOrEu[F].pure(a.unboxed)
      case Invalid(e) => AppExOrEu[F].raiseErrorOrEmpty(e.exception)
    }
  }
}

object Value {

  def apply(unboxed: String, ref: Option[String] = None): Value = Impls.ValueImpl(unboxed, ref)

  private object Impls {

    case class ValueImpl(unboxed: String, ref: Option[String]) extends Value {
      override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
        ValidatedValueImpls(unboxed, NonEmptyList.of(nrule, nrules: _*), ref)

      override def toString: String = Show[Value].show(this)
    }

    case class ValidatedValueImpls(unboxed: String, rules: NonEmptyList[ValidationRule], ref: Option[String])
        extends ValidatedValue {

      override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
        copy(rules = rules.concatNel(NonEmptyList.of(nrule, nrules: _*)))

      override def toString: String = Show[Value].show(this)
    }
  }

  //======================================= IMPLICITS ==================================
  implicit val advxmlValueCatsInstances: PartialOrder[Value] with Show[Value] = new PartialOrder[Value]
    with Show[Value] {
    override def partialCompare(x: Value, y: Value): Double = x.unboxed.compareTo(y.unboxed).toDouble
    override def show(t: Value): String = s"""${t.ref.map(r => s"$r => ").getOrElse("")}"${t.unboxed}""""
  }
}

class ValidationRule(val name: String, val validator: String => Boolean, val errorReason: String) {

  final def apply[T <: Value](v: T): Validated[ValidationRule.Error, T] = {
    validator(v.unboxed) match {
      case true  => Valid(v)
      case false => Invalid(ValidationRule.Error(this, errorReason))
    }
  }
}

object ValidationRule {

  def apply(name: String, validator: String => Boolean, errorReason: => String): ValidationRule =
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
