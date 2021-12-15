package com.itmo.microservices.demo.delivery.api.event

import java.util.*

data class ReserveSlotEvent(val orderId: UUID, val slot: Int){

}