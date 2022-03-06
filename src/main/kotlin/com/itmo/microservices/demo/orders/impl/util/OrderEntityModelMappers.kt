package com.itmo.microservices.demo.orders.impl.util

import com.itmo.microservices.demo.orders.api.model.BookingDto
import com.itmo.microservices.demo.orders.api.model.OrderDto
import com.itmo.microservices.demo.orders.api.model.PaymentLogRecordDto
import com.itmo.microservices.demo.orders.impl.entity.Order
import java.util.*

fun OrderDto.toEntity(userId: UUID) = Order(
    id = this.id,
    timeCreated = this.timeCreated,
    status = this.status,
    userId = userId,
    deliveryDuration = this.deliveryDuration
)
//
//fun Order.toModel(): OrderModel = OrderModel(
//    id = this.id,
//    basketId = this.basketId,
//    date = this.date,
//    userId = this.userId,
//    status = this.status
//)
fun Order.toDto(itemsMap: Map<UUID, Int>) : OrderDto = OrderDto(
    id = this.id!!,
    timeCreated = this.timeCreated,
    status = this.status,
    itemsMap = itemsMap,
    deliveryDuration = this.deliveryDuration,
    paymentHistory = listOf()
)
fun Order.toDto(itemsMap: Map<UUID, Int>,paymentHistory: List<PaymentLogRecordDto>) : OrderDto = OrderDto(
    id = this.id!!,
    timeCreated = this.timeCreated,
    status = this.status,
    itemsMap = itemsMap,
    deliveryDuration = this.deliveryDuration,
    paymentHistory = paymentHistory
)
fun Order.toBookingDto(failed : Set<UUID>) : BookingDto = BookingDto(
    id = this.id!!,
    failedItems = failed
)