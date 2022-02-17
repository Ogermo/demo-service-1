package com.itmo.microservices.demo.stock.impl.logging

import com.itmo.microservices.commonlib.logging.NotableEvent

enum class StockItemServiceNotableEvents(private val template: String) : NotableEvent {
    I_STOCK_ITEM_CREATED("Stock Item created: {}"),
    I_STOCK_ITEM_CHANGED("Stock Item changed: {}"),
    I_STOCK_ITEM_DELETED("Stock Item deleted: {}"),
    I_ORDER_QUERY("Query order: {}"),
    I_BOOKING_QUERY("Query booking: {}"),
    I_CHECK_BOOKING("Checking booking: {}");

    override fun getTemplate(): String {
        return template
    }

    override fun getName(): String {
        return name
    }
}