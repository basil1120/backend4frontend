package com.example.backend4frontend.web.config

import com.example.backend4frontend.util.converter.StringToTaskStatusConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToTaskStatusConverter())
    }
}