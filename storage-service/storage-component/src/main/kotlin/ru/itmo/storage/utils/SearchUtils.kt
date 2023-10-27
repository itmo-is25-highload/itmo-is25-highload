package ru.itmo.storage.utils

class SearchUtils {
    companion object {
        fun <U, T> rightBinSearch(arr: List<U>, value: T, func: (U, T) -> Boolean): Int {
            var left = 0
            var right = arr.size - 1
            while (left < right) {
                val mid = (left + right + 1) / 2
                if (func.invoke(arr[left], value)) {
                    left = mid
                } else {
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
