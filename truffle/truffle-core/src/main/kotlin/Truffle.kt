package com.wafflestudio.spring.truffle.core

import com.wafflestudio.spring.truffle.core.protocol.TruffleEvent

object Truffle {
    private lateinit var hub: IHub

    internal object HubAdapter : IHub {
        override fun captureEvent(truffleEvent: TruffleEvent) {
            Truffle.captureEvent(truffleEvent)
        }
    }

    @Synchronized fun init(apiKey: String): IHub {
        if (Truffle::hub.isInitialized) {
            return hub
        }

        validateConfig(apiKey)

        hub = Hub(apiKey)
        return HubAdapter
    }

    private fun captureEvent(truffleEvent: TruffleEvent) {
        hub.captureEvent(truffleEvent)
    }

    private fun validateConfig(apiKey: String) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Truffle API key is blank")
        }
    }
}
