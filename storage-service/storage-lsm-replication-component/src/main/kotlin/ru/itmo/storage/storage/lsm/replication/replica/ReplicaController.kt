package ru.itmo.storage.storage.lsm.replication.replica

import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.lsm.core.AVLTree

@RestController
class ReplicaController(
    private val keyValueRepository: KeyValueRepository
) {
    @PostMapping("sync")
    fun syncWithMaster(@RequestBody entry: AVLTree.Entry) {
        runBlocking {
            keyValueRepository.set(entry.key, entry.value)
        }
    }
}
