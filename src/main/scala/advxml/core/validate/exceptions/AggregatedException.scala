package advxml.core.validate.exceptions

import java.io.{OutputStreamWriter, PrintStream, PrintWriter}

import cats.data.NonEmptyList

/** Advxml
  * Created by geirolad on 11/07/2019.
  *
  * @author geirolad
  */
class AggregatedException(val exceptions: NonEmptyList[Throwable])
    extends RuntimeException(
      exceptions.toList
        .map(_.getMessage)
        .mkString(",\n")
    ) {

  def getStackTraces: Map[Throwable, Array[StackTraceElement]] =
    exceptions.toList.map(e => (e, e.getStackTrace)).toMap

  override def printStackTrace(): Unit = printStackTrace(System.err)

  override def printStackTrace(s: PrintStream): Unit =
    printStackTrace(new PrintWriter(new OutputStreamWriter(s)))

  override def printStackTrace(s: PrintWriter): Unit =
    exceptions.toList.foreach(e => {
      e.printStackTrace(s)
      s.print(s"\n\n${(0 to 70).map(_ => "#").mkString("")}\n\n")
    })

  /** @deprecated Use [[AggregatedException.getStackTraces]] instead */
  @Deprecated
  override def getStackTrace: Array[StackTraceElement] = super.getStackTrace

  /** @deprecated This method is not supported by [[AggregatedException]] */
  @Deprecated
  override def setStackTrace(stackTrace: Array[StackTraceElement]): Unit =
    throw new UnsupportedOperationException
}
