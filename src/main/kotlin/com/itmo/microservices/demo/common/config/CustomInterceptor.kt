package com.itmo.microservices.demo.common.config

import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.logging.CommonNotableEvents
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingRequestWrapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomInterceptor : HandlerInterceptor {

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        var requestCacheWrapperObject = ContentCachingRequestWrapper(request)
        requestCacheWrapperObject.parameterMap
        eventLogger.info(CommonNotableEvents.I_NEW_REQuEST,requestCacheWrapperObject.contentAsByteArray)
        return true
    }
}