package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree

interface MemtableFlushService {
    fun flush(memtable: AVLTree)
}
