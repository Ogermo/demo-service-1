package com.itmo.microservices.demo.stock.impl.entity

import com.itmo.microservices.demo.stock.api.model.BookingStatus
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "booking_log_record")
class BookingLogRecord {
    @Id
    var id: UUID? = null
    var bookingId : UUID = UUID.randomUUID()
    var itemId : UUID = UUID.randomUUID()
    var status: BookingStatus = BookingStatus.SUCCESS
    var amount: Int = 0
    var timestamp: Long = 0

    constructor()

    constructor(id: UUID? = null, bookingId: UUID, itemId: UUID,
                status: BookingStatus = BookingStatus.SUCCESS, amount: Int = 0,
    timestamp: Long = 0) {
        if (id == null){
            this.id = UUID.randomUUID()
        }
        else
        {
            this.id = id
        }
        this.bookingId = bookingId
        this.itemId = itemId
        this.status = status
        this.amount = amount
        this.timestamp = timestamp
    }
}