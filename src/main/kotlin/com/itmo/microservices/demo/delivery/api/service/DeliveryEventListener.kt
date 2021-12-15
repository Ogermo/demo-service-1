package com.itmo.microservices.demo.delivery.api.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.delivery.api.event.ReserveSlotEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class DeliveryEventListener {

    @Autowired
    private lateinit var deliveryService : DeliveryService

    @Autowired
    private lateinit var eventBus : EventBus

    @PostConstruct
    fun init(){
        eventBus.register(this)
    }

    @Subscribe
    fun onSlotReserve(event : ReserveSlotEvent){
        deliveryService.reserveDeliverySlots(event.orderId,event.slot)
    }
}