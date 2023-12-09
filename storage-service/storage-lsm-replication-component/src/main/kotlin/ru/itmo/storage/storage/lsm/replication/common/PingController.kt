package ru.itmo.storage.storage.lsm.replication.common

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ping")
class PingController {
    @GetMapping
    fun ping(): String {
        return "pong"
    }
}
