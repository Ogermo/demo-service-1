package com.itmo.microservices.demo.orders.api.event

import com.itmo.microservices.demo.delivery.api.model.Status
import java.util.*

data class SlotReserveReponseEvent(val orderId: UUID, val slot: Int, val status: Status)
