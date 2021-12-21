package com.itmo.microservices.demo.stock.api.controller

import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.api.service.BookingService
import com.itmo.microservices.demo.stock.impl.entity.BookingLogRecord
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping
class BookingController(private val bookingService: BookingService) {

    @GetMapping("/_internal/bookingHistory/{bookingId}")
    @Operation(
        summary = "Get booking records by booking id",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Bookings not found", responseCode = "404", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getBookingsByBookingId(@PathVariable bookingId: UUID): List<BookingLogRecord> =
        bookingService.getBookingsByBookingId(bookingId)

}