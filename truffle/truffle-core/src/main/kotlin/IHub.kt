package com.wafflestudio.spring.truffle.core

import com.wafflestudio.spring.truffle.core.protocol.TruffleEvent

interface IHub {
    fun captureEvent(truffleEvent: TruffleEvent)
}
