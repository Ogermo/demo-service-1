package com.itmo.microservices.demo.orders.api.event

import java.util.*

data class OrderPaidEvent (
    val id: UUID
        )
{}