package com.itmo.microservices.demo.orders.impl.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.delivery.api.model.Status
import com.itmo.microservices.demo.orders.api.event.OrderPaidEvent
import com.itmo.microservices.demo.orders.api.event.PaymentCreatedEvent
import com.itmo.microservices.demo.orders.api.event.SlotReserveReponseEvent
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.impl.repository.OrderItemsRepository
import com.itmo.microservices.demo.stock.api.event.DeleteItemEvent
import com.itmo.microservices.demo.stock.api.service.StockItemService
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class OrderEventListener {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var stockItemService: StockItemService

    @Autowired
    private lateinit var orderItemsRepository: OrderItemsRepository

    @Autowired
    private lateinit var eventBus: EventBus

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @PostConstruct
    fun init(){
        eventBus.register(this)
    }

    @Subscribe
    fun onSlotReserveResponse(event : SlotReserveReponseEvent){
        System.out.println("Response: " + event.status + " for order " + event.orderId + " time slot " + event.slot)
    }

    @Subscribe
    fun onPaymentCreated(event : PaymentCreatedEvent) {

        if (event.status == Status.SUCCESS) {

            eventBus.post(OrderPaidEvent(event.orderId))

            orderService.requestDeductStockItems(event.orderId)
        }

        System.out.println("Response: " + event.status + " for order " + event.orderId + " transaction id " + event.transactionID)
    }

    @Subscribe
    fun onDeletedItemFromCatalog(event : DeleteItemEvent){
        print("Need to check all carts and orders which contain item: " + event.title)
    }

    @Subscribe
    fun onOrderPaid(event: OrderPaidEvent) {

        val orderItems = orderItemsRepository.findByOrderId(event.id)
        var amountSum = 0.0

        for (item in orderItems) {

            val itemObj = stockItemService.getStockItemById(item.itemId!!)

            amountSum += item.amount!! * itemObj.price
        }

        meterRegistry.counter("revenue", "serviceName", "p04").increment(amountSum)

        orderService.changeOrderStatusToPaid(event.id)
    }
}