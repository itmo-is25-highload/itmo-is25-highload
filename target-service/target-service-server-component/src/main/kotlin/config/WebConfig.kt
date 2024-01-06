package ru.itmo.target.server.config


import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.itmo.target.server.middleware.EndpointUrlRateLimiterMiddleware
import ru.itmo.target.server.middleware.UserIPRateLimiterMiddleware

@Configuration
class WebConfig(private val endpointMiddleware: EndpointUrlRateLimiterMiddleware, private val userIpMiddleware: UserIPRateLimiterMiddleware) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(endpointMiddleware)
        registry.addInterceptor(userIpMiddleware)
    }
}
