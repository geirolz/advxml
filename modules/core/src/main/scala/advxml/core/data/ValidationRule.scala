package advxml.core.data
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

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
