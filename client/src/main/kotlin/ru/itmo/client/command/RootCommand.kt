package ru.itmo.client.command

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import ru.itmo.client.command.get.GetCommand
import ru.itmo.client.command.set.SetCommmand
import java.util.concurrent.Callable

@Service
@Command(
    name = "root",
    subcommands = [
        GetCommand::class,
        SetCommmand::class,
    ],
)
class RootCommand : Callable<Int> {
    private val log = KotlinLogging.logger { }

    override fun call(): Int {
        log.info { "Nothing happened" }
        return 0
    }
}
