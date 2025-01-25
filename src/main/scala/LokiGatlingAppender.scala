import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.Level
import com.github.loki4j.logback.Loki4jAppender
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter

class LokiGatlingAppender extends Loki4jAppender with Settings {

  override val gatlingLogSettings = new GatlingLogSettings()  // Added override modifier
  private val objectMapper = new ObjectMapper()

  override def start(): Unit = {
    super.start()
  }

  override def append(event: ILoggingEvent): Unit = {
    val targetLogger = event.getLoggerName

    // Process ONLY logs from the configured loggers (inverted logic)
    val loggerName = gatlingLogSettings.getLoggerName
    if (loggerName != null && loggerName != targetLogger) return

    val errorLoggerName = gatlingLogSettings.getErrorLoggerName
    if (errorLoggerName != null && errorLoggerName != targetLogger) return

    event.prepareForDeferredProcessing()
    if (gatlingLogSettings.isIncludeCallerData) event.getCallerData

    // Log original event for debugging
//    System.out.println(s"Original Event: ${event.getMessage}")

    val parsedEvent = parseEvent(event)

    // Log parsed event for debugging
//    System.out.println(s"Parsed Event: ${parsedEvent.getMessage}")


    val wrappedEvent = new LoggingEventWrapper(event, parsedEvent.getMessage)

//    System.out.println(s"Wrapped Event: ${wrappedEvent.getMessage}")

    super.append(wrappedEvent)
  }

  private def parseEvent(event: ILoggingEvent): ILoggingEvent = {
  val wsEvent = event.getLoggerName.contains("io.gatling.http.action.ws")
  val httpEvent = event.getLoggerName.contains("io.gatling.http.engine.response.DefaultStatsProcessor")
  val sessionHookEvent = event.getMessage.contains("Session:")

  val levelCondition = event.getLevel == Level.DEBUG || event.getLevel == Level.TRACE

//    System.out.println(s"httpEvent: $httpEvent")
//    System.out.println(s"wsEvent: $wsEvent")
//    System.out.println(s"levelCondition: $levelCondition")
//    System.out.println(s"sessionHookEvent: $sessionHookEvent")

  (httpEvent, wsEvent, levelCondition, sessionHookEvent) match {
    case (true, false, true, true) =>
//      System.out.println("Detected HTTP event.")
      formatHttpEvent(event)
    case (false, true, true, false) =>
//      System.out.println("Detected WebSocket event.")
      formatWsEvent(event)
    case (false, false, false, true) =>
//      System.out.println("Detected Session event.")
      formatSessionEvent(event)
    case _ =>
//      System.out.println(s"Unmatched Event: Logger = ${event.getLoggerName}, Message = ${event.getMessage}")
      event
  }
}


  private def formatHttpEvent(event: ILoggingEvent): ILoggingEvent = {
    val writer = new StringWriter()
    val gen = objectMapper.getFactory.createGenerator(writer)
    try {
      gen.writeStartObject()
      GatlingLogParser.httpFields(gen, event.getMessage, gatlingLogSettings.extractSessionAttributes)
      gen.writeEndObject()
      gen.close()
//      System.out.println(s"Formatted HTTP Event: ${writer.toString}")
      new LoggingEventWrapper(event, writer.toString)
    } catch {
      case e: Exception =>
        System.err.println(s"Error formatting HTTP Event: ${e.getMessage}")
        event
    }
  }

  private def formatWsEvent(event: ILoggingEvent): ILoggingEvent = {
    val writer = new StringWriter()
    val gen = objectMapper.getFactory.createGenerator(writer)
    try {
      gen.writeStartObject()
      GatlingLogParser.wsFields(gen, event.getMessage, gatlingLogSettings.extractSessionAttributes)
      gen.writeEndObject()
      gen.close()
//      System.out.println(s"Formatted WS Event: ${writer.toString}")
      new LoggingEventWrapper(event, writer.toString)
    } catch {
      case e: Exception =>
        System.err.println(s"Error formatting WS Event: ${e.getMessage}")
        event
    }
  }

  private def formatSessionEvent(event: ILoggingEvent): ILoggingEvent = {
  val writer = new StringWriter()
  val gen = objectMapper.getFactory.createGenerator(writer)
  try {
    gen.writeStartObject()
//    System.out.println(s"Processing session log: ${event.getMessage}")
    GatlingLogParser.sessionFields(gen, event.getMessage, gatlingLogSettings.extractSessionAttributes)
    gen.writeEndObject()
    gen.close()
//    System.out.println(s"Formatted Session Event: ${writer.toString}")
    new LoggingEventWrapper(event, writer.toString)
  } catch {
    case e: Exception =>
      System.err.println(s"Error formatting Session Event: ${e.getMessage}")
      event
  }
}

}