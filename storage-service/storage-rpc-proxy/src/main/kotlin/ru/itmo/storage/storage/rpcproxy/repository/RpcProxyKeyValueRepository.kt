package ru.itmo.storage.storage.rpcproxy.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.rpcproxy.service.ClientProvider
import java.util.zip.CRC32

@Service
class RpcProxyKeyValueRepository(
    private val clientProvider: ClientProvider,
) : KeyValueRepository {

    private val log = KotlinLogging.logger { }

    override suspend fun get(key: String): String {
        val crc = calculateCrc(key)

        return clientProvider.provide(crc).get(key)
    }

    override suspend fun set(key: String, value: String) {
        val crc = calculateCrc(key)

        clientProvider.provide(crc).set(key, value)
    }

    private fun calculateCrc(key: String): Long {
        val calculator = CRC32()
        calculator.update(key.toByteArray())

        log.info { "Crc computed by algorithm is ${calculator.value.toUInt().toString(16)}" }
        return calculator.value
    }
}

