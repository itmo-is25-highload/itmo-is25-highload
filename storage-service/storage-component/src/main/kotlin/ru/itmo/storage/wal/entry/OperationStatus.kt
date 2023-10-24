package ru.itmo.storage.wal.entry

enum class OperationStatus {
    PENDING,
    SUCCESS,
    FAIL,
    UNKNOWN,
}
