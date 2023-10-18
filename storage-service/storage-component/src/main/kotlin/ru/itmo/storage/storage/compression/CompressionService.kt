package ru.itmo.storage.storage.compression

interface CompressionService {
    fun compress(data: ByteArray): ByteArray

    fun decompress(compressedData: ByteArray): ByteArray
}
