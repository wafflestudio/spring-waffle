package com.wafflestudio.spring.truffle.core.protocol

data class TruffleEvent(
    val version: String = TruffleVersion.V1,
    val runtime: TruffleRuntime = truffleRuntime,
    val level: TruffleLevel,
    val exception: TruffleException,
) {
    companion object {
        val truffleRuntime: TruffleRuntime = TruffleRuntime()
    }
}

data class TruffleRuntime(
    val name: String = "Java",
    val version: String = System.getProperty("java.version"),
)
