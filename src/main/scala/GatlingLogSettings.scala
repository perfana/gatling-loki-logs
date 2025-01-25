class GatlingLogSettings {
  private var loggerName: String = _
  private var errorLoggerName: String = _
  private var includeCallerData: Boolean = false

  var extractSessionAttributes: Option[String] = None
  var excludeResources: Option[Boolean] = None
  def getLoggerName: String = loggerName
  def setLoggerName(name: String): Unit = loggerName = name

  def getErrorLoggerName: String = errorLoggerName
  def setErrorLoggerName(name: String): Unit = errorLoggerName = name

  def isIncludeCallerData: Boolean = includeCallerData
  def setIncludeCallerData(include: Boolean): Unit = includeCallerData = include

}
