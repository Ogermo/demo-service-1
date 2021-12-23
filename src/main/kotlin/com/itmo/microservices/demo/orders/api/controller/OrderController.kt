package com.itmo.microservices.demo.orders.api.controller

import com.google.common.eventbus.EventBus
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.delivery.api.event.ReserveSlotEvent
import com.itmo.microservices.demo.delivery.api.model.DeliveryModel
import com.itmo.microservices.demo.delivery.api.service.DeliveryService
import com.itmo.microservices.demo.orders.api.model.BookingDto
import com.itmo.microservices.demo.orders.api.model.OrderModel
import com.itmo.microservices.demo.orders.api.model.OrderModelDTO
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.impl.service.DefaultOrderService
import com.itmo.microservices.demo.orders.impl.entity.Order
import com.itmo.microservices.demo.orders.impl.repository.OrderRepository
import com.itmo.microservices.demo.orders.impl.util.toBookingDto
import com.itmo.microservices.demo.orders.impl.util.toEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import kong.unirest.json.JSONObject
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpServerErrorException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.HashMap

@RestController
class OrderController(private val orderService: OrderService,
                      private val orderRepository: OrderRepository,
                      private val deliveryService: DeliveryService,
                      private val eventBus: EventBus
) {

    @PostMapping("/orders")
    @Operation(
            summary = "Creates new order v2",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Service error", responseCode = "500", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun createOrder(@AuthenticationPrincipal user: UserDetails) = orderService.createOrder()

    @PutMapping("/orders/{order_id}/items/{item_id}")
    @Operation(
            summary = "Put items to cart",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Service error", responseCode = "500", content = [Content()]),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun putItemsToCart(@PathVariable order_id : UUID, @PathVariable item_id : UUID, @RequestParam(value = "amount") amount : Int = 1, @AuthenticationPrincipal user : UserDetails) = orderService.putItemToOrder(order_id, item_id, amount)


    @PostMapping("/orders/{order_id}/bookings")
    @Operation(
            summary = "Finalization and booking",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Service error", responseCode = "500", content = [Content()]),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun book(@PathVariable order_id : UUID, @AuthenticationPrincipal user : UserDetails) {
        if(orderService.book(order_id, user) == null){
            throw HttpServerErrorException(HttpStatus.BAD_REQUEST, "Service error")
        }
    }

    @PostMapping("/orders/{order_id}/delivery")
    @Operation(
            summary = "Choosing desired slot",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
                ApiResponse(description = "Service error", responseCode = "500", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun deliver(@PathVariable order_id: UUID, @RequestParam slot : Int): BookingDto {
        eventBus.post(ReserveSlotEvent(order_id,slot))
        var order = orderRepository.findByIdOrNull(order_id) ?: return Order().toBookingDto(setOf())
        return order.toBookingDto(setOf())
    }
    //replace me with events!

        @GetMapping("/orders/{order_id}")
        @Operation(
            summary = "Returns current order",
            responses = [
                ApiResponse(description = "OK", responseCode = "200"),
                ApiResponse(description = "Bad request", responseCode = "400", content = [Content()]),
                ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()]),
                ApiResponse(description = "Service error", responseCode = "500", content = [Content()])
            ],
            security = [SecurityRequirement(name = "bearerAuth")]
        )
        fun getOrder(@PathVariable order_id: UUID) = orderService.getOrder(order_id)
    }