package advxml.instances

import advxml.core.data.ThrowableNel
import cats.data.NonEmptyList
import cats.kernel.Semigroup

private[advxml] trait AllInstances extends AllCommonInstances with AllTransforInstances with ConverterInstances

private[instances] trait AllCommonInstances extends AggregatedExceptionInstances

//============================== EXCEPTIONS ==============================
private[instances] trait AggregatedExceptionInstances {
  implicit val semigroupInstanceForAggregatedException: Semigroup[Throwable] =
    (x: Throwable, y: Throwable) => ThrowableNel.toThrowable(NonEmptyList.of(x, y))
}
