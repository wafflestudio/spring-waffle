package com.wafflestudio.spring.truffle.core.protocol

data class TruffleEvent(
    val version: String = TruffleVersion.V1,
    val runtime: TruffleAppInfo.TruffleRuntime = TruffleAppInfo.runtime,
    val level: TruffleLevel,
    val exception: TruffleException,
)
