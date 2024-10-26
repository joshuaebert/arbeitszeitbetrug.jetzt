package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.rateLimitModule() {
    install(RateLimit) {
        handleGlobalRateLimit(this)
    }
}

fun handleGlobalRateLimit(config: RateLimitConfig) {
    config.global {
        rateLimiter(limit = 5, refillPeriod = 1.minutes)
    }
}