package ru.itmo.storage.storage.wal.entry

enum class OperationStatus {
    PENDING,
    SUCCESS,
    FAIL,
    UNKNOWN,
}
