package ru.itmo.storage.storage.lsm.bloomfilter

import java.util.BitSet
import kotlin.math.ln
import kotlin.math.min

class BloomFilter(maxSize: Int, expectedElements: Int = 0) {
    private val size: Int = min(maxSize, expectedElements)
    private val bitMask: BitSet = BitSet(size)
    private val hashFunctionsNumber: Long = getHashFunctionsNumber(expectedElements)

    fun add(value: String) {
        for (i in 0 until hashFunctionsNumber) {
            bitMask.set(getHash(value, i))
        }
    }

    fun isNotPresent(value: String): Boolean {
        for (i in 0 until hashFunctionsNumber) {
            if (!bitMask[getHash(value, i)]) {
                return true
            }
        }

        return false
    }

    private fun getHash(value: String, hashOrder: Long): Int {
        return getHash("$hashOrder$value")
    }

    private fun getHash(value: String): Int {
        var hash = 5381
        for (c in value) {
            hash = ((hash shl 5) + hash) + c.code
        }

        return hash.mod(size)
    }

    private fun getHashFunctionsNumber(expectedElements: Int) =
        if (expectedElements == 0) {
            0
        } else {
            Math.round((size.toDouble() / expectedElements) * ln(2.0))
        }
}
