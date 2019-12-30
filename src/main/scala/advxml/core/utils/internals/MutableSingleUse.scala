package advxml.core.utils.internals

import java.util.concurrent.atomic.AtomicBoolean

//TODO: Ugly -> to refactor
private[core] class MutableSingleUse[T] private[internals] (private val value: T) {

  private val used: AtomicBoolean = new AtomicBoolean(false)

  def getOrElse(orElseValue: => T): T =
    if (isUsed) {
      orElseValue
    } else {
      used.set(true)
      value
    }

  def isUsed: Boolean = used.get()
}

private[core] object MutableSingleUse {
  def apply[T](value: T): MutableSingleUse[T] = new MutableSingleUse(value)
}
