import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{IThrowableProxy, LoggerContextVO, StackTraceElementProxy}
import org.slf4j.{Marker, LoggerFactory}
import org.slf4j.event.KeyValuePair
import java.util.{List => JList, Map => JMap}

class LoggingEventWrapper(delegate: ILoggingEvent, newMessage: String) extends ILoggingEvent {

  // Override message with parsed JSON
  override def getMessage: String = newMessage

  // Delegate methods
  override def getFormattedMessage: String = newMessage
  override def getLoggerName: String = delegate.getLoggerName
  override def getLevel: Level = delegate.getLevel
  override def getTimeStamp: Long = delegate.getTimeStamp
  override def getThreadName: String = delegate.getThreadName
  override def getArgumentArray: Array[AnyRef] = delegate.getArgumentArray
  override def getThrowableProxy: IThrowableProxy = delegate.getThrowableProxy
  override def getCallerData: Array[StackTraceElement] = delegate.getCallerData
  override def hasCallerData: Boolean = delegate.hasCallerData
  override def getLoggerContextVO: LoggerContextVO = delegate.getLoggerContextVO
  override def getMDCPropertyMap: JMap[String, String] = delegate.getMDCPropertyMap

  // Deprecated method (but still required by ILoggingEvent)
//  @deprecated("Use getMDCPropertyMap", "1.4")
  override def getMdc: JMap[String, String] = delegate.getMdc

  // Required method
  override def prepareForDeferredProcessing(): Unit = delegate.prepareForDeferredProcessing()

  // New methods (SLF4J 2.0.x)
  override def getMarkerList: JList[Marker] = delegate.getMarkerList
  override def getKeyValuePairs: JList[KeyValuePair] = delegate.getKeyValuePairs
  override def getNanoseconds: Int = delegate.getNanoseconds
  override def getSequenceNumber: Long = delegate.getSequenceNumber
}