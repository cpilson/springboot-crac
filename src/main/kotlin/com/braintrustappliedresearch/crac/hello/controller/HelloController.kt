package com.braintrustappliedresearch.crac

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import org.springframework.beans.factory.annotation.Value

@RestController
class HelloController(
    @Value("\${ENV_SUPER_SECRET}")
    private val superSecret: String
) {

    @GetMapping("/hello")
    fun hello(): Mono<Map<String, Any>> = Mono.just(
        mapOf(
            "message" to "Hello, World! The secret word is: ${superSecret}",
            "timestamp" to System.currentTimeMillis()
        )
    )
}