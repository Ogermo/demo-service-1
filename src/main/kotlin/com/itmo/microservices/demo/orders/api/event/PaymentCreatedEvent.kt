package com.itmo.microservices.demo.orders.api.event

import com.itmo.microservices.demo.delivery.api.model.Status
import java.util.*

data class PaymentCreatedEvent(val orderId: UUID, val transactionID: UUID?, val status: Status)
