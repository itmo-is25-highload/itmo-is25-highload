package ru.itmo.target.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [TargetServiceApp::class])
class TargetServiceApp

fun main(args: Array<String>) {
    runApplication<TargetServiceApp>(*args)
}
