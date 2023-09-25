package ru.itmo.client.command

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import java.util.concurrent.Callable

@Service
@Command(
    name = "root",
    subcommands = [GetCommand::class],
)
class RootCommand : Callable<Int> {
    private val log = KotlinLogging.logger { }

    override fun call(): Int {
        log.info { "Nothing happened" }
        return 0
    }
}
