package com.wafflestudio.spring.truffle.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.wafflestudio.spring.truffle.core.IHub
import com.wafflestudio.spring.truffle.core.Truffle
import com.wafflestudio.spring.truffle.core.protocol.TruffleEvent
import com.wafflestudio.spring.truffle.core.protocol.TruffleException
import com.wafflestudio.spring.truffle.core.protocol.TruffleLevel

class TruffleAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    lateinit var apiKey: String
    private lateinit var hub: IHub

    override fun start() {
        hub = Truffle.init(apiKey)
        super.start()
    }

    override fun append(eventObject: ILoggingEvent) {
        if (!eventObject.loggerName.startsWith("com.wafflestudio.spring.truffle")) {
            val truffleEvent = createEvent(eventObject)
            hub.captureEvent(truffleEvent)
        }
    }

    private fun createEvent(eventObject: ILoggingEvent): TruffleEvent {
        val exception = eventObject.throwableProxy?.let {
            TruffleException((it as ThrowableProxy).throwable)
        } ?: TruffleException(
            className = eventObject.loggerName,
            message = eventObject.formattedMessage,
            elements = eventObject.callerData.map {
                TruffleException.Element(
                    className = it.className,
                    methodName = it.methodName,
                    fileName = it.fileName ?: "",
                    lineNumber = it.lineNumber,
                    isInAppInclude = true, // FIXME
                )
            },
        )

        return TruffleEvent(
            level = TruffleLevel.from(eventObject.level),
            exception = exception,
        )
    }
}
