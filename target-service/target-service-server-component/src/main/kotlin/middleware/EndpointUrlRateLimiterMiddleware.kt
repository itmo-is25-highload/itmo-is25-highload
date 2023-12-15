package middleware

import client.RateLimiterClient
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerInterceptor

@Service
class EndpointUrlRateLimiterMiddleware(private val rateLimiterClient: RateLimiterClient) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val url = request.requestURL.toString() // Скорее всего оно выдает немного не то что я имею ввиду, но я это проверю.
        if (!rateLimiterClient.limit("EndpointUrl", url)) {
            response.sendError(429)
            return false
        }
        return true
    }
}
