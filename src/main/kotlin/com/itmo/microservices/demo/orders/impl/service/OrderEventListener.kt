package com.itmo.microservices.demo.orders.impl.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.delivery.api.model.Status
import com.itmo.microservices.demo.orders.api.event.PaymentCreatedEvent
import com.itmo.microservices.demo.orders.api.event.SlotReserveReponseEvent
import com.itmo.microservices.demo.orders.api.service.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class OrderEventListener {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var eventBus: EventBus

    @PostConstruct
    fun init(){
        eventBus.register(this)
    }

    @Subscribe
    fun onSlotReserveResponse(event : SlotReserveReponseEvent){
        System.out.println("Response: " + event.status + " for order " + event.orderId + " time slot " + event.slot)
    }

    @Subscribe
    fun onPaymentCreated(event : PaymentCreatedEvent){
        System.out.println("Response: " + event.status + " for order " + event.orderId + " transaction id " + event.transactionID)
    }
}