package ru.itmo.storage.storage.utils

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class SearchUtils {
    companion object {
        fun <U, T> rightBinSearch(arr: List<U>, value: T, func: (U, T) -> Boolean): Int {
            log.info { "Right binary for value $value and data $arr" }
            var left = 0
            var right = arr.size - 1
            while (left < right) {
                log.info { "Right binary search iteration for indices ($left, $right]" }
                val mid = (left + right + 1) / 2

                log.info { "Middle value is $mid" }
                if (func.invoke(arr[mid], value)) {
                    log.info { "Func invoke is true. Upper left index to $mid" }
                    left = mid
                } else {
                    log.info { "Func invoke is false. Lower right index to ${mid - 1}" }
                    right = mid - 1
                }
            }

            return when (func.invoke(arr[left], value)) {
                true -> left
                false -> -1
            }
        }
    }
}
