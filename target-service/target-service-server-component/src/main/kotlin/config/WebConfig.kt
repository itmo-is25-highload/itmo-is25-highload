package config

import middleware.EndpointUrlRateLimiterMiddleware
import middleware.UserIPRateLimiterMiddleware
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig(private val endpointMiddleware: EndpointUrlRateLimiterMiddleware, private val userIpMiddleware: UserIPRateLimiterMiddleware) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(endpointMiddleware)
        registry.addInterceptor(userIpMiddleware)
    }
}