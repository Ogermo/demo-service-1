package com.itmo.microservices.demo.stock.impl.repository

import com.itmo.microservices.demo.stock.api.model.BookingLogRecordModel
import com.itmo.microservices.demo.stock.impl.entity.BookingLogRecord
import com.itmo.microservices.demo.stock.impl.entity.StockItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BookingRepository : JpaRepository<BookingLogRecord, UUID> {
    @Query("From BookingLogRecord WHERE bookingId = ?1")
    fun findByBookingId(id: UUID) : List<BookingLogRecord>

    @Query("From BookingLogRecord WHERE bookingId = ?1 AND itemId = ?2")
    fun findItem(bookingId : UUID, itemId : UUID) : BookingLogRecord?
}