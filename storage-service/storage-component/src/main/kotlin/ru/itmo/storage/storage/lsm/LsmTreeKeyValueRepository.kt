package ru.itmo.storage.storage.lsm

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.exception.KeyNotFoundException
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree

@Service
class LsmTreeKeyValueRepository : KeyValueRepository {

    private val tree: AVLTree = DefaultAVLTree()

    override fun get(key: String): String {
        return tree.find(key) ?: throw KeyNotFoundException(key)
    }

    override fun set(key: String, value: String) {
        tree.upsert(key, value)
    }
}
