package com.itmo.microservices.demo.orders.impl.logging

import com.itmo.microservices.commonlib.logging.NotableEvent

enum class OrderServiceNotableEvents(private val template: String) : NotableEvent {
    I_ORDER_CREATED("Order created: {}"),
    I_ORDER_DELETED("Order deleted: {}"),
    I_PAYMENT_ASSIGNED("Payment assigned: {}"),
    I_ORDER_ADDED("Added item: {}"),
    I_ORDER_CHECKED("Checking order: {}"),
    I_ORDER_DESCRIPTION("Order: {}"),
    I_ORDER_BOOKED("Booking order: {}");

    override fun getTemplate(): String {
        return template
    }

    override fun getName(): String {
        return name
    }
}