package ru.itmo.client

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import picocli.CommandLine
import picocli.CommandLine.IFactory
import ru.itmo.client.command.RootCommand
import ru.itmo.storage.client.config.StorageClientStubConfiguration

@SpringBootApplication(scanBasePackageClasses = [ClientApp::class])
@Import(
    StorageClientStubConfiguration::class,
)
class ClientApp(
    private val factory: IFactory,
    private val rootCommand: RootCommand,
) : CommandLineRunner, ExitCodeGenerator {
    private var exitCode: Int = 0

    override fun run(vararg args: String?) {
        exitCode = CommandLine(rootCommand, factory).execute(*args)
    }

    override fun getExitCode(): Int = exitCode
}

fun main(args: Array<String>) {
    runApplication<ClientApp>(*args)
}
