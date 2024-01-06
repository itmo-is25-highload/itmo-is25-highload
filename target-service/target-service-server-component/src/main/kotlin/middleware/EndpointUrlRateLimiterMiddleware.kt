package ru.itmo.target.server.middleware

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerInterceptor
import ru.itmo.ratelimit.client.RateLimiterClient
import org.springframework.web.util.UrlPathHelper

@Service
class EndpointUrlRateLimiterMiddleware(private val rateLimiterClient: RateLimiterClient) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val url = UrlPathHelper().getPathWithinApplication(request)
        if (!rateLimiterClient.limit("EndpointUrl", url)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
            return false
        }
        return true
    }
}
