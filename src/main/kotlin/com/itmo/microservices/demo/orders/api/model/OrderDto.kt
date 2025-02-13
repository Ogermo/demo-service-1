package com.itmo.microservices.demo.orders.api.model

import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.impl.entity.PaymentLogRecord
import java.util.*

data class OrderDto (
    val id: UUID,
    val timeCreated: Long,
    var status: OrderStatus,
    val itemsMap: Map<UUID, Int>,
    val deliveryDuration: Int?,
    val paymentHistory: List<PaymentLogRecord>
)