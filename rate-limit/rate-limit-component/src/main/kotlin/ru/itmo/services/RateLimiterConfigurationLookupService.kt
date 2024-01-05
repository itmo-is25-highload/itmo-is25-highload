package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.ratelimit.component.entries.DescriptorLimit

@Service
interface RateLimiterConfigurationLookupService {
    fun getLimit(key: String, value: String?): DescriptorLimit?
}