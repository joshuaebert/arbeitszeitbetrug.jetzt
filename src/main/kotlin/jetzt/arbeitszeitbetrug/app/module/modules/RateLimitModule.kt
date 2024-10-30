package jetzt.arbeitszeitbetrug.app.module.modules

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.rateLimitModule() {
    install(RateLimit) {
        setGlobalRateLimit()
    }
}

fun RateLimitConfig.setGlobalRateLimit() {
    global {
        rateLimiter(limit = 5, refillPeriod = 1.minutes)
    }
}