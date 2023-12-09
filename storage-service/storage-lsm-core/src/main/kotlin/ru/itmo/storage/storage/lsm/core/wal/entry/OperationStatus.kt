package ru.itmo.storage.storage.lsm.core.wal.entry

enum class OperationStatus {
    PENDING,
    SUCCESS,
    FAIL,
    UNKNOWN,
}
