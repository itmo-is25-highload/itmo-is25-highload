package ru.itmo.target.client.commands

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import ru.itmo.target.client.commands.griddy.DeviousGriddyCommand
import ru.itmo.target.client.commands.griddy.McGriddyCommand
import java.util.concurrent.Callable

@Component
@Command(
    name = "root",
    subcommands = [
        DeviousGriddyCommand::class,
        McGriddyCommand::class,
    ],
)
class RootCommand : Callable<Int> {
    private val log = KotlinLogging.logger { }

    override fun call(): Int {
        log.info { "Nothing happened" }
        return 0
    }
}
