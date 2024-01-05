package ru.itmo.target.client.commands.griddy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import ru.itmo.target.client.TargetServiceClient
import java.util.concurrent.Callable

@Service
@Command(name = "devious")
class DeviousGriddyCommand(private val client: TargetServiceClient) : Callable<Int> {
    private val log = KotlinLogging.logger { }

    override fun call(): Int {
        log.info { "Trying to hit devious griddy" }

        client.deviousGriddy()

        return 0
    }
}
