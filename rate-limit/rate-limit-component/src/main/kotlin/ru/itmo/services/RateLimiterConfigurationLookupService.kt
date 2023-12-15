package ru.itmo.services

import org.springframework.stereotype.Service
import ru.itmo.entries.DescriptorLimit

@Service
interface RateLimiterConfigurationLookupService {
    fun getLimit(key: String, value: String?): DescriptorLimit?
}