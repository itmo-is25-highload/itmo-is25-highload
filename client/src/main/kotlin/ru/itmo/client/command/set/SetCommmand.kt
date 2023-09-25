package ru.itmo.client.command.set

import org.springframework.stereotype.Service
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import ru.itmo.client.service.StorageService
import java.util.concurrent.Callable

@Service
@Command(name = "set")
class SetCommmand(
    private val storageService: StorageService,
) : Callable<Int> {

    @Parameters(index = "0")
    lateinit var key: String

    @Parameters(index = "1")
    lateinit var value: String

    override fun call(): Int {
        storageService.setValue(key, value)

        return 0
    }
}
