package com.wafflestudio.spring.truffle.core.protocol

data class TruffleException(
    val className: String,
    val message: String?,
    val elements: List<TruffleExceptionElement>,
)

data class TruffleExceptionElement(
    val className: String,
    val methodName: String,
    val lineNumber: Int,
    val fileName: String,
    val isInAppInclude: Boolean,
)

fun TruffleException(e: Throwable): TruffleException =
    TruffleException(
        className = e.javaClass.name,
        message = e.message,
        elements =
            e.stackTrace.map {
                TruffleExceptionElement(
                    className = it.className,
                    methodName = it.methodName,
                    lineNumber = it.lineNumber,
                    fileName = it.fileName ?: "",
                    // FIXME
                    isInAppInclude = true,
                )
            },
    )
