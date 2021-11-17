package advxml.data

import advxml.ApplicativeThrowOrEu
import cats.{Applicative, Id, PartialOrder, Show}
import cats.data.{NonEmptyList, Validated}
import cats.data.Validated.{Invalid, Valid}

import scala.util.matching.Regex

//======================================= VALUES ==================================
sealed trait Value extends AsValidable[ValidatedValue] {
  val ref: Option[String]
}
object Value {

  // instances
  implicit def valueExtractorForValueF[F[_]: ApplicativeThrowOrEu]: ValueExtractor[F, Value] = {
    case v @ SimpleValue(_, _)       => v.extract[F]
    case v @ ValidatedValue(_, _, _) => v.extract[F]
  }

  // embedded syntax
  implicit class ValueExtractorSyntaxOps[V <: Value](value: V) {

    def get(implicit be: ValueExtractor[Id, V]): String =
      be.extract(value)

    def validated(implicit ve: ValueExtractor[ValidatedNelThrow, V]): ValidatedNelThrow[String] =
      extract[ValidatedNelThrow]

    def extract[F[_]](implicit be: ValueExtractor[F, V]): F[String] =
      be.extract(value)
  }
}

//=============================== KINDS ===============================
case class SimpleValue(private[data] val data: String, ref: Option[String] = None)
    extends Value
    with Comparable[SimpleValue] {

  override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
    ValidatedValue.fromSimpleValue(this, NonEmptyList.of(nrule, nrules*))

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
      override def partialCompare(x: SimpleValue, y: SimpleValue): Double =
        x.data.compareTo(y.data).toDouble
      override def show(t: SimpleValue): String =
        s"""${t.ref.map(r => s"$r => ").getOrElse("")}"${t.data}""""
    }
}

case class ValidatedValue(
  private val data: String,
  rules: NonEmptyList[ValidationRule],
  ref: Option[String] = None
) extends Value {

  def toSimpleValue: SimpleValue = SimpleValue(data, ref)

  override def validate(nrule: ValidationRule, nrules: ValidationRule*): ValidatedValue =
    copy(rules = rules.concatNel(NonEmptyList.of(nrule, nrules*)))

  override def toString: String = Show[SimpleValue].show(toSimpleValue)
}
object ValidatedValue {

  def fromSimpleValue(
    simpleValue: SimpleValue,
    rules: NonEmptyList[ValidationRule]
  ): ValidatedValue =
    ValidatedValue(simpleValue.get, rules, simpleValue.ref)

  implicit def valueExtractorForValidatedValueF[F[_]: ApplicativeThrowOrEu]
    : ValueExtractor[F, ValidatedValue] =
    (vvalue: ValidatedValue) => {
      import cats.implicits.*

      val result: Validated[ValidationRule.Errors, SimpleValue] = vvalue.rules
        .map(validationRule => validationRule(vvalue.toSimpleValue).toValidatedNel)
        .toList
        .reduce((a, b) => a.productL[SimpleValue](b))
        .swap
        .map(errors => ValidationRule.Errors(vvalue, errors))
        .swap

      result match {
        case Valid(a)   => ApplicativeThrowOrEu[F].pure(a.get)
        case Invalid(e) => ApplicativeThrowOrEu[F].raiseErrorOrEmpty(e.exception)
      }
    }
}

//=============================== VALIDATION RULE ===============================
trait AsValidable[V] {

  def validate(nrule: ValidationRule, nrules: ValidationRule*): V

  def nonEmpty: V = validate(ValidationRule.NonEmpty)

  def matchRegex(regex: Regex): V = validate(ValidationRule.MatchRegex(regex))
}
