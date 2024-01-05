package ru.itmo.target.client

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import picocli.CommandLine
import ru.itmo.target.client.commands.RootCommand

@SpringBootApplication(scanBasePackageClasses = [TargetClientApp::class])
class TargetClientApp(
    private val factory: CommandLine.IFactory,
    private val rootCommand: RootCommand,
) : CommandLineRunner, ExitCodeGenerator {

    private var exitCode: Int = 0

    fun main(args: Array<String>) {
        runApplication<TargetClientApp>(*args)
    }

    override fun run(vararg args: String?) {
        exitCode = CommandLine(rootCommand, factory).execute(*args)
    }

    override fun getExitCode(): Int {
        return exitCode
    }
}
