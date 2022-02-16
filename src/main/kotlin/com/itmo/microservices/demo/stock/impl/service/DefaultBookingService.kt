package com.itmo.microservices.demo.stock.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.stock.api.model.BookingLogRecordModel
import com.itmo.microservices.demo.stock.api.model.BookingStatus
import com.itmo.microservices.demo.stock.api.service.BookingService
import com.itmo.microservices.demo.stock.impl.entity.BookingLogRecord
import com.itmo.microservices.demo.stock.impl.logging.StockItemServiceNotableEvents
import com.itmo.microservices.demo.stock.impl.repository.BookingRepository
import com.itmo.microservices.demo.stock.impl.util.toDto
import com.itmo.microservices.demo.stock.impl.util.toEntity
import com.itmo.microservices.demo.stock.impl.util.toModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Suppress("UnstableApiUsage")
@Service
class DefaultBookingService(private val bookingRepository: BookingRepository,
                            private val eventBus: EventBus) : BookingService{
    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    override fun getBookingsByBookingId(id: UUID): List<BookingLogRecord> {
        eventLogger.info(StockItemServiceNotableEvents.I_CHECK_BOOKING,id)
        return bookingRepository.findByBookingId(id)
    }

    override fun getBookingById(id: UUID): BookingLogRecord? {
        return bookingRepository.findByIdOrNull(id)
    }

    override fun createBooking(bookingModel: BookingLogRecordModel) : BookingLogRecordModel {
            bookingRepository.save(bookingModel.toEntity())
            return bookingModel
    }

    override fun changeBookingStatus(id: UUID, status : BookingStatus) {
        val booking = bookingRepository.findByIdOrNull(id) ?: return

        booking.status = status
        bookingRepository.save(booking)
    }
}