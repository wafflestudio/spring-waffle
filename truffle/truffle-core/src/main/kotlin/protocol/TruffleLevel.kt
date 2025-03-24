package com.wafflestudio.spring.truffle.core.protocol

import ch.qos.logback.classic.Level

enum class TruffleLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    FATAL,
    ;

    companion object {
        fun from(level: Level): TruffleLevel {
            return when {
                level.isGreaterOrEqual(Level.ERROR) -> ERROR
                level.isGreaterOrEqual(Level.WARN) -> WARNING
                level.isGreaterOrEqual(Level.INFO) -> INFO
                else -> DEBUG
            }
        }
    }
}
