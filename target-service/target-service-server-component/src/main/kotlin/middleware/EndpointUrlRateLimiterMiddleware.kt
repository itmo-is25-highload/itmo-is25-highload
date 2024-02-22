package ru.itmo.target.server.middleware

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UrlPathHelper
import ru.itmo.contracts.ratelimit.RateLimiterResponse
import ru.itmo.ratelimit.client.RateLimiterClient

@Service
class EndpointUrlRateLimiterMiddleware(private val rateLimiterClient: RateLimiterClient) : HandlerInterceptor {

    private var response: RateLimiterResponse? = null

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val url = UrlPathHelper().getPathWithinApplication(request)
        val result = rateLimiterClient.checkLimit("EndpointUrl", url)

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

        val url = UrlPathHelper().getPathWithinApplication(request)
        rateLimiterClient.incrementLimit("EndpointUrl", url)
    }
}
