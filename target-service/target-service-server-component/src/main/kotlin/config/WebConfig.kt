package ru.itmo.target.server.config


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.itmo.target.server.middleware.EndpointUrlRateLimiterMiddleware
import ru.itmo.target.server.middleware.UserIPRateLimiterMiddleware

@Configuration
@EnableWebMvc
@EnableConfigurationProperties(WebConfig::class)
class WebConfig(@Autowired private val endpointMiddleware: EndpointUrlRateLimiterMiddleware, @Autowired private val userIpMiddleware: UserIPRateLimiterMiddleware) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(endpointMiddleware)
        registry.addInterceptor(userIpMiddleware)
    }
}
