package controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class ServiceController {
    private val log = KotlinLogging.logger { }

    @GetMapping("/griddy/devious")
    fun hitDeviousGriddy(): String {
        log.info { "Hittin' dat devious griddy" }

        return "Devious griddy"
    }

    @GetMapping("/griddy/mc")
    fun hitMcGriddy(): String {
        log.info { "Bro hit dat mcgriddy :skull emoji:" }

        return "Mcgriddy :skull emoji:"
    }
}
