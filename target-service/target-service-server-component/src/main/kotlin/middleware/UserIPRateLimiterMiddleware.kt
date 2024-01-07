package ru.itmo.target.server.middleware

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import ru.itmo.contracts.ratelimit.RateLimiterResponse
import ru.itmo.ratelimit.client.RateLimiterClient

@Service
class UserIPRateLimiterMiddleware(private val rateLimiterClient: RateLimiterClient) : HandlerInterceptor {

    private var response: RateLimiterResponse? = null
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip: String? = request.getHeader("X-Forwarded-For") ?: request.remoteAddr
        val result = rateLimiterClient.checkLimit("UserIP", ip)

        response.addHeader("requests-left", result.requestsLeft.toString())

        if (!result.isAllowed) {
            response.addHeader("retry-after-seconds", result.secondsLeftTillReset.toString())
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
            return false
        }

        this.response = result
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        if (!this.response!!.isLimited) {
            return
        }
        val ip = request.getHeader("X-Forwarded-For") ?: request.remoteAddr
        rateLimiterClient.incrementLimit("UserIP", ip)
    }
}
