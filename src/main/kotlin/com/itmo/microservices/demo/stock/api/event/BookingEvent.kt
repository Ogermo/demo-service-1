package com.itmo.microservices.demo.stock.api.event

import com.itmo.microservices.demo.stock.api.model.BookingStatus
import java.util.*

data class BookingEvent(val bookingId: UUID,
                        val itemId : UUID,
                        val status : BookingStatus,
                        val amount: Int, val timestamp: Long) {
}