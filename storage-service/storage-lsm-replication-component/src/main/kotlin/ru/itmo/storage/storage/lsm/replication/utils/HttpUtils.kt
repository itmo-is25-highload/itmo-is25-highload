package ru.itmo.storage.storage.lsm.replication.utils

import org.springframework.web.context.request.RequestContextHolder

import org.springframework.web.context.request.ServletRequestAttributes




class HttpUtils {
    companion object {

        private val ipHeaders =
            arrayOf(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
            )

        fun getRequestIP(): String {
            if (RequestContextHolder.getRequestAttributes() == null) {
                return "0.0.0.0"
            }

            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)!!.request
            for (header in ipHeaders) {
                val ipList = request.getHeader(header)
                if (ipList != null && ipList.isNotEmpty() && !"unknown".equals(ipList, ignoreCase = true)) {
                    val address = ipList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    return address.replace("0:0:0:0:0:0:0:1", "127.0.0.1")
                }
            }

            return request.remoteAddr
        }
    }
}
