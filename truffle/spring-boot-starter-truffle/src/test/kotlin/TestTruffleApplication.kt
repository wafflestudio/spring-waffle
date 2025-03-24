package com.wafflestudio.truffle.sdk

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class TestTruffleApplication {
    @RestController
    class TestController {
        @GetMapping("/test")
        fun test(): String {
            throw RuntimeException("test")
        }
    }
}

@AutoConfigureWebTestClient
@SpringBootTest()
class TruffleReactiveTest(
    @Autowired private val webTestClient: WebTestClient,
) {
    @Test
    fun truffleTest() {
        webTestClient.get()
            .uri("/test")
            .exchange()
            .expectStatus()
            .is5xxServerError

        Thread.sleep(3000)
    }
}
