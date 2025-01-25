# Gatling Loki Logs

Logger which parses raw Gatling logs and sends them to Loki.

## Motivation

By default, Gatling writes logs to the console, which is inconvenient for analysing, collecting and storing information.
In fact, metrics don't contain the details of errors, they only have request status OK or KO.
When a metric occurs with an error it's impossible to figure out what happened: a check failed? got 404? or it was 502? etc.
Also, if you run load tests in the distributed mode, it will store your logs in separate injectors.
This logger allows getting all useful information so that it will be possible to correlate with your metrics.

To recap, the Logger is solving two main problems:

- Distributed metrics sending and storing them
- You can build a Graph with errors details for correlation by metrics

## Install

### Maven:

```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.3</version>
</dependency>
<dependency>
  <groupId>com.github.loki4j</groupId>
  <artifactId>loki-logback-appender</artifactId>
  <version>1.4.2</version>
```

### SBT

I provide minimal configuration, but you can add additional things what you need
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <appender name="LOKI" class="LokiGatlingAppender">
            <level>${logLevel}</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <url>http://${elkUrl}/_bulk</url>

        <!-- Loki connection settings -->
        <url>http://${lokiUrl}/loki/api/v1/push</url>
        <batchSize>100</batchSize>
        <batchTimeoutMs>1000</batchTimeoutMs>

        <!-- Optional authentication -->
        <username>${LOKI_USERNAME}</username>
        <password>${LOKI_PASSWORD}</password>

        <!-- Labels configuration -->
        <label>
            <pattern>app=gatling,level=%level,logger=%logger</pattern>
        </label>

        <!-- Message format -->
        <message>
            <pattern>%msg</pattern>
        </message>
    </appender>

    <logger name="io.gatling.http.engine.response" level="${logLevel}"/>
    <logger name="io.gatling.http.action.ws.fsm.WsLogger" level="${logLevel}"/>

    <root level="WARN">
        <appender-ref ref="LOKI"/>
    </root>
</configuration>
```
