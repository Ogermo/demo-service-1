package com.itmo.microservices.demo.stock.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BookingLogRecordModel(
    val bookingId : UUID,
    val itemId : UUID,
    val status: BookingStatus,
    val amount: Int,
    val timestamp: Long
)