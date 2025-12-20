package com.wafflestudio.spring.truffle.core

import com.wafflestudio.spring.truffle.core.protocol.TruffleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.util.Timeout
import org.slf4j.LoggerFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.util.concurrent.Executors

internal interface TruffleClient {
    fun sendEvent(truffleEvent: TruffleEvent)
}

internal class DefaultTruffleClient(
    apiKey: String,
    restClientBuilder: RestClient.Builder,
) : TruffleClient {
    private val events = MutableSharedFlow<TruffleEvent>(extraBufferCapacity = 10)
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        val coroutineScope =
            CoroutineScope(
                Executors.newSingleThreadExecutor {
                        r ->
                    Thread(r, "truffle-client").apply { isDaemon = true }
                }.asCoroutineDispatcher(),
            )

        val connectionManager =
            PoolingHttpClientConnectionManager().apply {
                maxTotal = 3
                defaultMaxPerRoute = 3
                setDefaultConnectionConfig(
                    ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofSeconds(5))
                        .setConnectTimeout(Timeout.ofSeconds(5))
                        .build(),
                )
            }

        val httpClient =
            HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        val restClient =
            restClientBuilder
                .requestFactory(requestFactory)
                .baseUrl("https://truffle-api.wafflestudio.com")
                .defaultHeader("x-api-key", apiKey)
                .build()

        coroutineScope.launch(SupervisorJob()) {
            events.collect {
                runCatching {
                    restClient
                        .post()
                        .uri("/events")
                        .body(it)
                        .retrieve()
                        .toBodilessEntity()
                }.getOrElse {
                    logger.warn("Failed to request to truffle server", it)
                }
            }
        }
    }

    override fun sendEvent(truffleEvent: TruffleEvent) {
        events.tryEmit(truffleEvent)
    }
}
