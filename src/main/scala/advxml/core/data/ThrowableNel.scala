package advxml.core.data

import advxml.core.data.error.AggregatedException
import cats.data.NonEmptyList

object ThrowableNel {

  def toThrowable(tnel: ThrowableNel): Throwable = AggregatedException(tnel)

  def fromThrowable(tnel: Throwable): ThrowableNel = tnel match {
    case ex: AggregatedException => ex.exceptions
    case ex                      => NonEmptyList.one(ex)
  }
}
