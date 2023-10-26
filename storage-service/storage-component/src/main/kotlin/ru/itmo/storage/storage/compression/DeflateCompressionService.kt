package ru.itmo.storage.storage.compression

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.compression.properties.DeflateCompressionServiceProperties
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

@Service
class DeflateCompressionService(private val properties: DeflateCompressionServiceProperties) : CompressionService {
    override fun compress(data: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(data)
        deflater.finish()
        val buffer = ByteArray(properties.allocatedBufferSize)
        val baos = ByteArrayOutputStream()
        val compressedSize = deflater.deflate(buffer)
        baos.write(buffer, 0, compressedSize)

        return baos.toByteArray()
    }

    override fun decompress(compressedData: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(compressedData)
        val buffer = ByteArray(properties.allocatedBufferSize)
        val baos = ByteArrayOutputStream()
        val inflatedSize = inflater.inflate(buffer)
        baos.write(buffer, 0, inflatedSize)

        return baos.toByteArray()
    }
}
