package com.wafflestudio.spring.truffle

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.wafflestudio.spring.truffle.appender.TruffleAppender
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.ApplicationListener

class TruffleApplicationListener : ApplicationListener<ApplicationPreparedEvent> {
    override fun onApplicationEvent(event: ApplicationPreparedEvent) {
        val property = event.applicationContext.environment.getProperty("logging.config", "classpath:logback-spring.xml")
        val logbackConfigFile = event.applicationContext.getResource(property)
        if (logbackConfigFile.exists()) {
            return
        }

        val truffleKey = event.applicationContext.environment.getProperty("truffle.client.api-key")
        if (truffleKey != null) {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            val logger: Logger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            val truffleAppender = createTruffleAppender(context, truffleKey)

            logger.addAppender(truffleAppender)
        }
    }

    private fun createTruffleAppender(
        context: LoggerContext,
        apiKey: String,
    ): Appender<ILoggingEvent> {
        val errorFilter = ThresholdFilter()
        errorFilter.setLevel("ERROR")
        errorFilter.start()

        val truffleAppender = TruffleAppender()
        truffleAppender.context = context
        truffleAppender.name = "TRUFFLE_ERROR_APPENDER"
        truffleAppender.apiKey = apiKey
        truffleAppender.addFilter(errorFilter)
        truffleAppender.start()

        return truffleAppender
    }
}
