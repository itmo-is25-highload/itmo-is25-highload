package ru.itmo.ratelimit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [RateLimitApp::class])
class RateLimitApp

fun main(args: Array<String>) {
    runApplication<RateLimitApp>(*args)
}
