package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.orders.api.model.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

interface OrderService {
    fun createOrder(userId: UUID) : OrderDto
    fun putItemToOrder(orderId : UUID, itemId : UUID, amount : Int) : ResponseEntity<Nothing>
    fun getOrder(orderId: UUID) :OrderDto
//    fun getOrdersByUsername(user : UserDetails) : List<OrderModel>
//    fun getOrder(orderId : UUID) : OrderModel
    fun book(orderId : UUID, user : UserDetails) : BookingDto?
    fun requestDeductStockItems(orderId: UUID)
    fun deleteOrder(orderId : UUID) : Boolean
    fun changeOrderStatusToPaid(orderId: UUID)
//    fun assignPayment(orderId : UUID, payment : PaymentModel)
}