package ru.itmo.target.server.middleware

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerInterceptor
import ru.itmo.ratelimit.client.RateLimiterClient

// Кажется что я здесь использую что-то очень и очень старое.
@Service
class UserIPRateLimiterMiddleware(private val rateLimiterClient: RateLimiterClient) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip: String? = request.getHeader("X-Forwarded-For")
        if (!rateLimiterClient.limit("UserIP", ip)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
            return false
        }
        return true
    }
}
