package ru.itmo.client.command

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.storage.client.StorageClient
import java.util.concurrent.Callable

@Service
@Command(name = "get")
class GetCommand(
    private val storageClient: StorageClient,
) : Callable<Int> {

    private val log = KotlinLogging.logger { }

    @Parameters(index = "0")
    private var key: String = ""

    override fun call(): Int {
        log.info { "get command call with key: $key" }

        runCatching {
            storageClient.get(key)
        }.fold(
            onSuccess = { log.info { "Success" } },
            onFailure = { log.info { "Failure" } },
        )

        return 0
    }
}
