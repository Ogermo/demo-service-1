package com.itmo.microservices.demo.orders.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.orders.api.messaging.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.messaging.OrderDeletedEvent
import com.itmo.microservices.demo.orders.api.model.OrderModel
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.impl.logging.OrderServiceNotableEvents
import com.itmo.microservices.demo.orders.impl.repository.OrderRepository
import com.itmo.microservices.demo.orders.impl.util.toEntity
import com.itmo.microservices.demo.orders.impl.util.toModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class DefaultOrderService(private val orderRepository: OrderRepository,
                              private val eventBus: EventBus) : OrderService {

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    override fun getOrdersByUserId(userName: String): List<OrderModel> {
        val orders = orderRepository.findAll()
        val result = mutableListOf<OrderModel>()
        for (order in orders) {
            if(order.userName == userName) {
                result.add(order.toModel())
            }
        }
        return result
    }

    override fun getOrder(orderId: UUID): OrderModel {
        return orderRepository.findByIdOrNull(orderId)?.toModel() ?: throw NotFoundException("Order $orderId not found")
    }

    override fun addOrder(order: OrderModel, userName : String) {
        orderRepository.save(order.toEntity().also { it.userName = userName })
        eventBus.post(OrderCreatedEvent(order))
        eventLogger.info(OrderServiceNotableEvents.I_ORDER_CREATED, order.toEntity())
    }

    override fun deleteOrder(orderId: UUID) {
        val order = orderRepository.findByIdOrNull(orderId) ?: throw NotFoundException("Order $orderId not found")
        eventBus.post(OrderDeletedEvent(order.toModel()))
        eventLogger.info(OrderServiceNotableEvents.I_ORDER_DELETED, order)
        orderRepository.deleteById(orderId)
    }
}