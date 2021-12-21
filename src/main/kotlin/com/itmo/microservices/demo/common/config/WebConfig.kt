package com.itmo.microservices.demo.common.config

import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

//@Configuration
//class WebConfig : WebMvcConfigurer{
//
//    override fun addInterceptors(registry: InterceptorRegistry) {
//        registry.addInterceptor(CustomInterceptor())
//    }
//}