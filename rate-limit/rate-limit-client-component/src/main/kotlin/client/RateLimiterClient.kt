package client

interface RateLimiterClient {
    fun limit(key: String, value: String?): Boolean
}