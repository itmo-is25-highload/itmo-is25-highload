package ru.itmo.client.command.get

import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.client.service.StorageService
import java.util.concurrent.Callable

@Service
@Command(name = "get")
class GetCommand(
    private val storageService: StorageService,
) : Callable<Int> {

    @Parameters(index = "0")
    lateinit var key: String

    override fun call(): Int {
        storageService.getValue(key)

        return 0
    }
}
